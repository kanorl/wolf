package com.frost.entity.db.persistence

import com.frost.entity.Entity
import com.frost.entity.db.Repository

internal abstract class PersistTask(repository: Repository, entity: Entity<*>, callback: (() -> Unit)? = null) : Runnable {
    companion object {
        fun saveTask(repository: Repository, entity: Entity<*>, callback: (() -> Unit)? = null): PersistTask = Save(repository, entity)
        fun updateTask(repository: Repository, entity: Entity<*>, callback: (() -> Unit)? = null): PersistTask = Update(repository, entity)
        fun remvoeTask(repository: Repository, entity: Entity<*>, callback: (() -> Unit)? = null): PersistTask = Remove(repository, entity)
    }
}

internal class Save(val repository: Repository, val entity: Entity<*>, callback: (() -> Unit)? = null) : PersistTask(repository, entity) {
    override fun run() {
        repository.save(entity)
    }
}

internal class Update(val repository: Repository, val entity: Entity<*>, callback: (() -> Unit)? = null) : PersistTask(repository, entity) {
    override fun run() {
        repository.update(entity)
    }
}

internal class Remove(val repository: Repository, val entity: Entity<*>, callback: (() -> Unit)? = null) : PersistTask(repository, entity) {
    override fun run() {
        repository.remove(entity)
    }
}
