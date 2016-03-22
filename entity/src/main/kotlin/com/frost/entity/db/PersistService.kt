package com.frost.entity.db

import com.frost.common.concurrent.NamedThreadFactory
import com.frost.common.concurrent.lock.lock
import com.frost.common.concurrent.shutdownAndAwaitTermination
import com.frost.common.lang.abs
import com.frost.common.logging.getLogger
import com.frost.entity.EntitySetting
import com.frost.entity.IEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

interface PersistService {

    fun save(entity: IEntity<*>, callback: (() -> Unit)? = null)

    fun update(entity: IEntity<*>, callback: (() -> Unit)? = null)

    fun remove(entity: IEntity<*>, callback: (() -> Unit)? = null)
}

@Component
internal class ImmediatePersistService : PersistService {

    @Autowired
    private lateinit var persistence: Persistence
    @Autowired
    private lateinit var setting: EntitySetting
    private lateinit var executors: Array<ExecutorService>

    @PostConstruct
    private fun init() {
        assert(setting.persistPoolSize > 0 && (setting.persistPoolSize and (setting.persistPoolSize - 1)) == 0, { "persist pool size must be positive and power of 2" })
        executors = (0 until setting.persistPoolSize).map { Executors.newSingleThreadExecutor(NamedThreadFactory("persistence")) }.toTypedArray()
    }

    override fun save(entity: IEntity<*>, callback: (() -> Unit)?) {
        submit(PersistTask.saveTask(persistence, entity, callback))
    }

    override fun update(entity: IEntity<*>, callback: (() -> Unit)?) {
        submit(PersistTask.updateTask(persistence, entity, callback))
    }

    override fun remove(entity: IEntity<*>, callback: (() -> Unit)?) {
        submit(PersistTask.removeTask(persistence, entity, callback))
    }

    internal fun submit(task: PersistTask) {
        executors[(task.entity.id.hashCode().abs() and (executors.size - 1))].submit(task)
    }
}

@Component
internal class ScheduledPersistService : PersistService {
    private val logger by getLogger()

    @Autowired
    private lateinit var persistence: Persistence
    @Autowired
    private lateinit var delegate: ImmediatePersistService
    @Autowired
    private lateinit var setting: EntitySetting

    private val cache = ConcurrentHashMap<Class<*>, ConcurrentHashMap<Any, PersistTask>>()
    private val lock = ReentrantReadWriteLock()
    private val r = lock.readLock()
    private val w = lock.writeLock()
    private val scheduler = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("persist-scheduler"))

    @PostConstruct
    private fun init() {
        scheduler.scheduleWithFixedDelay({ persist() }, setting.persistInterval, setting.persistInterval, TimeUnit.SECONDS)
    }

    @PreDestroy
    private fun onDestroy() {
        scheduler.shutdownAndAwaitTermination()
        persist()
    }


    override fun save(entity: IEntity<*>, callback: (() -> Unit)?) {
        val map = mapFor(entity)
        val task = PersistTask.saveTask(persistence, entity, callback)
        r.lock { map.put(entity.id, task) }
    }

    override fun update(entity: IEntity<*>, callback: (() -> Unit)?) {
        val map = mapFor(entity)
        val task = PersistTask.saveTask(persistence, entity, callback)
        r.lock { map.putIfAbsent(entity.id, task) }
    }

    override fun remove(entity: IEntity<*>, callback: (() -> Unit)?) {
        val map = mapFor(entity)
        val task = PersistTask.saveTask(persistence, entity, callback)
        r.lock { map.put(entity.id, task) }
    }

    private fun mapFor(entity: IEntity<*>): ConcurrentHashMap<Any, PersistTask> = cache[entity.javaClass] ?: cache.computeIfAbsent(entity.javaClass, { ConcurrentHashMap() })

    private fun persist() {
        var tasks = emptyList<PersistTask>()
        w.lock {
            tasks = cache.values.flatMap { it.values }.toList()
            cache.values.clear()
        }
        tasks.forEach { delegate.submit(it) }

        logger.debug("{} PersistTask submitted.", tasks.size)
    }
}
