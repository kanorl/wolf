package com.frostwolf.entity.cache

import com.frostwolf.common.concurrent.task
import com.frostwolf.common.lang.max
import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.scheduling.Scheduler
import com.frostwolf.common.time.seconds
import com.frostwolf.common.time.toDuration
import com.frostwolf.common.toJson
import com.frostwolf.entity.Entity
import com.frostwolf.entity.EntitySetting
import com.frostwolf.entity.db.PersistService
import com.frostwolf.entity.db.Querier
import com.google.common.base.Function
import com.google.common.cache.*
import com.google.common.collect.Sets.newConcurrentHashSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
internal class EntityCacheImpl<ID : Comparable<ID>, E : Entity<ID>>(private val clazz: Class<E>) : EntityCache<ID, E> {
    private val logger by getLogger()

    @Autowired
    private lateinit var querier: Querier
    @Autowired
    private lateinit var setting: EntitySetting
    @Autowired
    private lateinit var scheduler: Scheduler
    @Autowired
    private lateinit var persistService: PersistService

    private val removing = newConcurrentHashSet<ID>()
    private val updating = ConcurrentHashMap<ID, E>()

    private val dbLoader = CacheLoader.from(Function<ID, E> {
        val loaded = querier.one(it!!, clazz)
        if (removing.contains(it)) null else updating.remove(it) ?: loaded
    })
    private lateinit var cache: LoadingCache<ID, E>

    @PostConstruct
    private fun init() {
        cache = CacheBuilder.from(cacheBuilderSpec()).removalListener<ID, E> (RemovalListener {
            if (it.wasEvicted() && it.value!!.edited()) {
                updating.put(it.key!!, it.value!!)
            }
            if (!it.wasEvicted()) {
                removing.add(it.key)
                persistService.remove(it.value!!, { removing.remove(it.key) })
            }
        }).build(dbLoader)

        val annotation = clazz.getAnnotation(CacheSpec::class.java)
        val where = annotation.preLoad
        if (where.isNotEmpty()) {
            querier.query(clazz, where).forEach { cache.put(it.id, it) }
        }
        val interval = if (annotation.persistInterval.isEmpty()) setting.persistInterval else annotation.persistInterval
        val duration = max(interval.toDuration(), 5.seconds)
        scheduler.scheduleWithFixedDelay(duration, task(name = "${clazz.simpleName} persist") { updateEdited() })
    }

    fun updateEdited() {
        cache.asMap().values.filter { it.edited() }.forEach { update(it) }
    }

    private fun cacheBuilderSpec(): CacheBuilderSpec {
        val cacheSpec = clazz.getAnnotation(CacheSpec::class.java)
        val maximumSize = ((if (cacheSpec.size > 0) cacheSpec.size else setting.cacheSize) * cacheSpec.sizeFactor).toInt()
        val concurrencyLevel = if (maximumSize >= setting.cacheSize) 16 else 4
        val spec = "initialCapacity=$maximumSize, maximumSize=$maximumSize, concurrencyLevel=$concurrencyLevel, expireAfterAccess=30m${if (setting.cacheStat) ", recordStats" else ""}"
        return CacheBuilderSpec.parse(spec)
    }

    override fun get(id: ID): E? = try {
        cache[id]
    } catch(e: CacheLoader.InvalidCacheLoadException) {
        null
    } catch(e: Exception) {
        logger.error(e.message, e)
        throw e
    }

    override fun getOrCreate(id: ID, factory: (ID) -> E): E = try {
        cache[id, {
            val loaded = dbLoader.load(id)
            val entity = loaded ?: factory(id)
            if (removing.contains(id)) {
                throw IllegalStateException("${clazz.simpleName}[$id] is removed")
            }
            if (entity !== loaded) {
                check(entity.id == id, { "Created entity's id does not match: expected[$id], given[${entity.id}]" })
                persistService.save(entity)
            }
            entity
        }]
    } catch(e: Exception) {
        logger.error(e.message, e)
        throw e
    }

    override fun update(entity: E) = if (removing.contains(entity.id)) {
        logger.error("Cannot update a removing entity {}[{}]", clazz.simpleName, entity.toJson())
    } else {
        persistService.update(entity)
    }

    override fun remove(id: ID) {
        get(id)?.let { cache.invalidate(it.id) }
    }
}