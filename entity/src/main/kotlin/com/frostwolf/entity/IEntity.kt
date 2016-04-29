package com.frostwolf.entity

import com.frostwolf.entity.cache.CacheSpec
import com.google.common.collect.ComparisonChain
import org.springframework.data.annotation.Transient
import java.util.concurrent.atomic.AtomicInteger

@CacheSpec
abstract class IEntity<T : Comparable<T>> : Comparable<IEntity<T>> {
    @Transient
    @kotlin.jvm.Transient
    private var dbVersion = AtomicInteger(0)
    @Transient
    @kotlin.jvm.Transient
    private var editVersion = AtomicInteger(0)

    abstract var id: T
        protected set

    final fun save(): IEntity<T> {
        editVersion.incrementAndGet()
        return this
    }

    final fun postSave() {
        assert(editVersion.get() >= dbVersion.get(), { "editVersion < dbVersion" })
        val dbV = dbVersion.get()
        val eV = editVersion.get()
        val set = dbVersion.compareAndSet(dbV, editVersion.get())
        check(set, { "Failed to update db version: from $dbV to $eV" })
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