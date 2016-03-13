package com.frost.entity.db.persistence

import com.frost.common.concurrent.NamedThreadFactory
import com.frost.common.concurrent.lock.lock
import com.frost.common.lang.abs
import com.frost.entity.Entity
import com.frost.entity.db.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock

internal abstract class Persistence(val repository: Repository) {

    abstract fun save(entity: Entity<*>, callback: (() -> Unit)? = null)

    abstract fun update(entity: Entity<*>, callback: (() -> Unit)? = null)

    abstract fun remove(entity: Entity<*>, callback: (() -> Unit)? = null)
}

internal class PersistenceImpl(repository: Repository, nThread: Int) : Persistence(repository) {

    init {
        assert(nThread > 0 && (nThread and (nThread - 1)) == 0, { "nThread must be positive and power of 2" })
    }

    private val executors = (0 until nThread).map { Executors.newSingleThreadExecutor(NamedThreadFactory("persistence")) }.toTypedArray()

    override fun save(entity: Entity<*>, callback: (() -> Unit)?) {
        executorFor(entity).submit { PersistTask.saveTask(repository, entity, callback) }
    }

    override fun update(entity: Entity<*>, callback: (() -> Unit)?) {
        executorFor(entity).submit { PersistTask.updateTask(repository, entity, callback) }
    }

    override fun remove(entity: Entity<*>, callback: (() -> Unit)?) {
        executorFor(entity).submit { PersistTask.remvoeTask(repository, entity, callback) }
    }

    private fun executorFor(entity: Entity<*>): ExecutorService = executors[(entity.id.hashCode().abs() and (executors.size - 1))]
}

internal class ScheduledPersistenceProcessor(repository: Repository, delegate: PersistenceImpl, interval: Long) : Persistence(repository) {

    private val tasks = ConcurrentHashMap<Class<*>, ConcurrentHashMap<Any, PersistTask>>()
    private val lock = ReentrantReadWriteLock()
    private val r = lock.readLock()
    private val w = lock.writeLock()

    override fun save(entity: Entity<*>, callback: (() -> Unit)?) {
        val map = mapFor(entity)
        val task = PersistTask.saveTask(repository, entity, callback)
        r.lock { map.put(entity.id, task) }
    }

    override fun update(entity: Entity<*>, callback: (() -> Unit)?) {
        val map = mapFor(entity)
        val task = PersistTask.saveTask(repository, entity, callback)
        r.lock { map.putIfAbsent(entity.id, task) }
    }

    override fun remove(entity: Entity<*>, callback: (() -> Unit)?) {
        val map = mapFor(entity)
        val task = PersistTask.saveTask(repository, entity, callback)
        r.lock { map.put(entity.id, task) }
    }

    private fun mapFor(entity: Entity<*>): ConcurrentHashMap<Any, PersistTask> = tasks[entity.javaClass] ?: tasks.computeIfAbsent(entity.javaClass, { ConcurrentHashMap() })
}
