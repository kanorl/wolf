package com.frostwolf.entity.db

import com.frostwolf.entity.Entity

internal abstract class PersistTask(val persistence: Persistence, val entity: Entity<*>, val callback: (() -> Unit)?) : Runnable {
    companion object {
        fun saveTask(persistence: Persistence, entity: Entity<*>, callback: (() -> Unit)? = null): PersistTask = Save(persistence, entity.save(), callback)
        fun updateTask(persistence: Persistence, entity: Entity<*>, callback: (() -> Unit)? = null): PersistTask = Update(persistence, entity.save(), callback)
        fun removeTask(persistence: Persistence, entity: Entity<*>, callback: (() -> Unit)? = null): PersistTask = Remove(persistence, entity.save(), callback)
    }

    override final fun run() {
        if (!entity.edited()) {
            return;
        }
        persist()
        entity.postSave()
        callback?.invoke()
    }

    protected abstract fun persist();
}

internal class Save(persistence: Persistence, entity: Entity<*>, callback: (() -> Unit)?) : PersistTask(persistence, entity, callback) {
    override fun persist() {
        persistence.save(entity)
    }
}

internal class Update(persistence: Persistence, entity: Entity<*>, callback: (() -> Unit)?) : PersistTask(persistence, entity, callback) {
    override fun persist() {
        persistence.update(entity)
    }
}

internal class Remove(persistence: Persistence, entity: Entity<*>, callback: (() -> Unit)?) : PersistTask(persistence, entity, callback) {
    override fun persist() {
        persistence.remove(entity)
    }
}
