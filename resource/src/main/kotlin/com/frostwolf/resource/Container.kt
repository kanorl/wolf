package com.frostwolf.resource

import com.frostwolf.common.lang.random
import com.frostwolf.resource.validation.DuplicateResourceException
import java.util.*

interface Container<T : Resource> {
    fun contains(id: Int): Boolean = get(id) != null
    operator fun get(id: Int): T?
    fun first(): T?
    fun last(): T?
    fun next(current: Int): T?
    fun prev(current: Int): T?
    fun list(filter: ((T) -> Boolean)? = null): List<T>
    fun random(): T?
}

internal class ContainerImpl<T : Resource> : Container<T> {
    private val totalWeight: Int
    private val map: Map<Int, T>
    internal val set: NavigableSet<T>

    constructor(beans: List<T>) {
        beans.forEach { it.afterPropertiesSet() }
        val duplicated = beans.groupBy { it.id }.filterValues { it.size > 1 }.keys
        if (duplicated.size > 0) {
            throw DuplicateResourceException(duplicated.first().javaClass, duplicated)
        }
        totalWeight = beans.sumBy { it.weight() }
        map = beans.associateBy { it.id }
        set = TreeSet<T>(beans)
    }

    override operator fun get(id: Int): T? = map[id]

    override fun first(): T? = set.first()

    override fun last(): T? = set.last()

    override fun next(current: Int): T? = map[current]?.let { set.higher(it) }

    override fun prev(current: Int): T? = map[current]?.let { set.lower(it) }

    override fun list(filter: ((T) -> Boolean)?): List<T> = if (filter == null) set.toList() else set.filter { filter(it) }

    override fun random(): T? = set.random (totalWeight) { it.weight() }
}

internal class DelegatingContainer<T : Resource> : Container<T> {
    internal lateinit var delegatee: Container<T>

    override fun get(id: Int): T? = delegatee[id]

    override fun first(): T? = delegatee.first()

    override fun last(): T? = delegatee.last()

    override fun next(current: Int): T? = delegatee.next(current)

    override fun prev(current: Int): T? = delegatee.prev(current)

    override fun list(filter: ((T) -> Boolean)?): List<T> = delegatee.list(filter)

    override fun random(): T? = delegatee.random()
}