package com.frostwolf.entity.cache

import com.frostwolf.entity.Entity

interface EntityCache<K : Comparable<K>, E : Entity<K>> {

    operator fun get(id: K): E?

    fun getOrCreate(id: K, factory: (K) -> E): E

    fun update(entity: E)

    fun remove(id: K)
}