package com.frost.entity

import com.frost.entity.cache.CacheSpec
import com.google.common.collect.ComparisonChain
import org.springframework.data.annotation.Transient
import java.util.concurrent.atomic.AtomicInteger

@CacheSpec
abstract class IEntity<T : Comparable<T>> : Comparable<IEntity<T>> {
    @Transient
    private var dbVersion = AtomicInteger(0)
    @Transient
    private var editVersion = AtomicInteger(0)

    abstract var id: T

    final fun markEdited(): IEntity<T> {
        editVersion.incrementAndGet()
        return this
    }

    final fun unmarkEdited() {
        assert(editVersion.get() >= dbVersion.get(), { "editVersion < dbVersion" })
        dbVersion.set(editVersion.get())
    }

    final fun edited() = editVersion.get() > dbVersion.get()

    final override fun compareTo(other: IEntity<T>): Int = ComparisonChain.start().compare(this.javaClass.name, other.javaClass.name).compare(this.id, other.id).result()

    override fun equals(other: Any?): Boolean = when {
        other == null -> false
        this === other -> true
        this.javaClass != other.javaClass -> false
        else -> this.id == (other as IEntity<*>).id
    }

    override fun hashCode(): Int = id.hashCode()
}