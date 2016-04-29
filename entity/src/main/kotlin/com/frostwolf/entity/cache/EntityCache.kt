package com.frostwolf.entity.cache

import com.frostwolf.entity.IEntity

interface EntityCache<K : Comparable<K>, E : IEntity<K>> {

    operator fun get(id: K): E?

    fun getOrCreate(id: K, factory: (K) -> E): E

    fun update(entity: E)

    fun remove(id: K)
}