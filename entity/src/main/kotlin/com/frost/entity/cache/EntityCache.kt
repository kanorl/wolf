package com.frost.entity.cache

import com.frost.entity.AbstractEntity

interface EntityCache<K : Comparable<K>, E : AbstractEntity<K>> {

    operator fun get(id: K): E?

    fun getOrCreate(id: K, factory: (K) -> E): E

    fun update(entity: E)

    fun remove(id: K)
}