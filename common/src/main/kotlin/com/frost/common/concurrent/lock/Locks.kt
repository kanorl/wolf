package com.frost.common.concurrent.lock

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock

private class OrderedLock<T>(val unsorted: List<T>) : Lock where T : Lock, T : Comparable<T> {

    val sorted = unsorted.sorted()
    override fun lock() {
        sorted.forEach { it.lock() }
    }

    override fun unlock() {
        var index = sorted.lastIndex
        while (index > 0) {
            sorted[index].unlock()
            index--
        }
    }

    override final fun lockInterruptibly() {
        throw UnsupportedOperationException()
    }

    override final fun newCondition(): Condition {
        throw UnsupportedOperationException()
    }

    override final fun tryLock(): Boolean {
        throw UnsupportedOperationException()
    }

    override final fun tryLock(time: Long, unit: TimeUnit): Boolean {
        throw UnsupportedOperationException()
    }
}

inline fun lock(vararg elements: Any, op: () -> Unit) = getLock(*elements).lock(op)

inline fun Lock.lock(op: () -> Unit) {
    lock()
    try {
        op()
    } finally {
        unlock()
    }
}

private val lockCache = CacheBuilder.newBuilder().maximumSize(1000).weakKeys().weakValues().build<Any, ComparableLock>(CacheLoader.from<Any, ComparableLock> { ComparableLock(it!!) })

fun getLock(vararg elements: Any): Lock {
    return when (elements.size) {
        0 -> throw IllegalArgumentException("Empty array")
        1 -> lockCache.get(elements[0])
        else -> OrderedLock(elements.map { obj -> lockCache.get(obj) })
    }
}