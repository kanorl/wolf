package com.frost.common.concurrent.lock

import java.util.concurrent.locks.ReentrantLock
import kotlin.comparisons.compareValues

class ComparableLock(val obj: Any, fair: Boolean = false) : ReentrantLock(fair), Comparable<ComparableLock> {

    private val order = if (obj is Comparable<*>) 0 else System.identityHashCode(obj)

    override fun compareTo(other: ComparableLock): Int {
        if (this.obj is Comparable<*> && other.obj !is Comparable<*>) {
            return 1
        }
        if ( this.obj !is Comparable<*> && other.obj is Comparable<*>) {
            return -1
        }

        val result = if (this.obj is Comparable<*> && other.obj is Comparable<*>) {
            compareValues(this.obj, other.obj)
        } else {
            compareValues(this.order, other.order)
        }
        return if (result == 0) compareValues(this.obj.javaClass.name, other.obj.javaClass.name) else result
    }
}