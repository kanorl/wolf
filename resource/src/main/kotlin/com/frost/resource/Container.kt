package com.frost.resource

import java.util.*

interface Container<T : Resource> {
    fun contains(id: Int): Boolean = get(id) != null
    operator fun get(id: Int): T?
    fun first(): T?
    fun last(): T?
    fun next(current: T): T?
    fun prev(current: T): T?
    fun list(filter: ((T) -> Boolean)? = null): List<T>
    fun random(): T?
}

internal class ContainerImpl<T : Resource>(beans: List<T>) : Container<T> {
    val totalWeight = beans.sumBy { it.weight() }
    val map = beans.associateBy { it.getId() }
    val sorted = TreeSet<T>(beans)

    override operator fun get(id: Int): T? = map[id]

    override fun first(): T? = sorted.first()

    override fun last(): T? = sorted.last()

    override fun next(current: T): T? = sorted.higher(current)

    override fun prev(current: T): T? = sorted.lower(current)

    override fun list(filter: ((T) -> Boolean)?): List<T> = if (filter == null) sorted.toList() else sorted.filter { filter(it) }

    override fun random(): T? = com.frost.common.random(sorted, totalWeight) { it.weight() }
}

internal class DelegatingContainer<T : Resource>() : Container<T> {
    internal lateinit var delegatee: Container<T>

    override fun get(id: Int): T? = delegatee[id]

    override fun first(): T? = delegatee.first()

    override fun last(): T? = delegatee.last()

    override fun next(current: T): T? = delegatee.next(current)

    override fun prev(current: T): T? = delegatee.prev(current)

    override fun list(filter: ((T) -> Boolean)?): List<T> = delegatee.list(filter)

    override fun random(): T? = delegatee.random()
}