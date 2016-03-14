package com.frost.entity.db

import com.frost.entity.AbstractEntity

internal abstract class PersistTask(val Persistence: Persist, val entity: AbstractEntity<*>, val callback: (() -> Unit)?) : Runnable {
    companion object {
        fun saveTask(Persistence: Persist, entity: AbstractEntity<*>, callback: (() -> Unit)? = null): PersistTask = Save(Persistence, entity.markEdited(), callback)
        fun updateTask(Persistence: Persist, entity: AbstractEntity<*>, callback: (() -> Unit)? = null): PersistTask = Update(Persistence, entity.markEdited(), callback)
        fun removeTask(Persistence: Persist, entity: AbstractEntity<*>, callback: (() -> Unit)? = null): PersistTask = Remove(Persistence, entity.markEdited(), callback)
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

internal class Save(Persistence: Persist, entity: AbstractEntity<*>, callback: (() -> Unit)?) : PersistTask(Persistence, entity, callback) {
    override fun persist() {
        Persistence.save(entity)
    }
}

internal class Update(Persistence: Persist, entity: AbstractEntity<*>, callback: (() -> Unit)?) : PersistTask(Persistence, entity, callback) {
    override fun persist() {
        Persistence.update(entity)
    }
}

internal class Remove(Persistence: Persist, entity: AbstractEntity<*>, callback: (() -> Unit)?) : PersistTask(Persistence, entity, callback) {
    override fun persist() {
        Persistence.remove(entity)
    }
}
