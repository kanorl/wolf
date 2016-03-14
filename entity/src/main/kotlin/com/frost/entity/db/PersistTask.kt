package com.frost.entity.db

import com.frost.entity.AbstractEntity

internal abstract class PersistTask(val Persistence: Persistence, val entity: AbstractEntity<*>, val callback: (() -> Unit)?) : Runnable {
    companion object {
        fun saveTask(Persistence: Persistence, entity: AbstractEntity<*>, callback: (() -> Unit)? = null): PersistTask = Save(Persistence, entity.markEdited(), callback)
        fun updateTask(Persistence: Persistence, entity: AbstractEntity<*>, callback: (() -> Unit)? = null): PersistTask = Update(Persistence, entity.markEdited(), callback)
        fun removeTask(Persistence: Persistence, entity: AbstractEntity<*>, callback: (() -> Unit)? = null): PersistTask = Remove(Persistence, entity.markEdited(), callback)
    }

    override final fun run() {
        if (!entity.edited()) {
            return;
        }
        persist()
        entity.unmarkEdited()
        callback?.invoke()
    }

    protected abstract fun persist();
}

internal class Save(Persistence: Persistence, entity: AbstractEntity<*>, callback: (() -> Unit)?) : PersistTask(Persistence, entity, callback) {
    override fun persist() {
        Persistence.save(entity)
    }
}

internal class Update(Persistence: Persistence, entity: AbstractEntity<*>, callback: (() -> Unit)?) : PersistTask(Persistence, entity, callback) {
    override fun persist() {
        Persistence.update(entity)
    }
}

internal class Remove(Persistence: Persistence, entity: AbstractEntity<*>, callback: (() -> Unit)?) : PersistTask(Persistence, entity, callback) {
    override fun persist() {
        Persistence.remove(entity)
    }
}
