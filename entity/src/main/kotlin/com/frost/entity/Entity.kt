package com.frost.entity

import com.frost.entity.cache.CacheSpec
import com.google.common.collect.ComparisonChain
import java.util.concurrent.atomic.AtomicInteger

@CacheSpec
abstract class Entity<T : Comparable<T>> : Comparable<Entity<T>> {

    @Transient
    private var dbVersion = AtomicInteger(0)
    @Transient
    private var editVersion = AtomicInteger(0)

    abstract var id: T

    final fun save() = editVersion.incrementAndGet()

    final fun changed() = editVersion.get() > dbVersion.get()

    final fun dbVersion(version: Int) = dbVersion.set(version)

    final override fun compareTo(other: Entity<T>): Int = ComparisonChain.start().compare(this.id, other.id).compare(this.javaClass.name, other.javaClass.name).result()

    override fun equals(other: Any?): Boolean = when {
        other == null -> false
        this === other -> true
        this.javaClass != other.javaClass -> false
        else -> this.id == (other as Entity<*>).id
    }

    override fun hashCode(): Int = id.hashCode()
}