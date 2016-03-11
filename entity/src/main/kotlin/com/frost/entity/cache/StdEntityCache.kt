package com.frost.entity.cache

import com.frost.entity.Entity
import com.frost.entity.db.Repository

class StdEntityCache<K : Comparable<K>, E : Entity<K>>(clazz: Class<Entity<K>>, val repository: Repository) : EntityCache<K, E> {

    override fun get(id: K): E? {
        throw UnsupportedOperationException()
    }

    override fun getOrCreate(id: K, factory: (K) -> E): E {

        throw UnsupportedOperationException()
    }

    override fun update(entity: E) {
    }

    override fun remove(entity: E) {
        throw UnsupportedOperationException()
    }
}