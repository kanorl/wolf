package com.frost.entity.cache

import com.frost.common.logging.getLogger
import com.frost.common.toJson
import com.frost.entity.Entity
import com.frost.entity.db.Repository
import com.google.common.base.Function
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.RemovalListener
import com.google.common.collect.Sets.newConcurrentHashSet

//@Suppress("UNCHECKED_CAST")
class StdEntityCache<ID : Comparable<ID>, E : Entity<ID>>(private val clazz: Class<E>, private val repository: Repository) : EntityCache<ID, E> {
    private val logger by getLogger()

    private val removing = newConcurrentHashSet<ID>()
    private val dbLoader = CacheLoader.from(Function<ID, E> {
        val loaded = repository.get(it, clazz)
        loaded?.let { if (removing.contains(it.id)) null else it }
    })
    private val cache = CacheBuilder.from("").removalListener<ID, E> (RemovalListener {
        if(it.wasEvicted()){

        }
        removing.add(it.key)
    }).build(dbLoader)

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
                assert(entity.id === id, {"Created entity's id is missing match: expected[$id], given[${entity.id}]"})
                repository.save(entity)
            }
            entity
        }]
    } catch(e: Exception) {
        logger.error(e.message, e)
        throw e
    }

    override fun update(entity: E) = if (removing.contains(entity.id)) {
        logger.error("Cannot update a removing entity [{]]", entity.toJson())
    } else {
        repository.update(entity)
    }

    override fun remove(entity: E) {
        throw UnsupportedOperationException()
    }
}

