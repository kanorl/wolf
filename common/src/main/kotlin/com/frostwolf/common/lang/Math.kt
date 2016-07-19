package com.frostwolf.common.lang

import kotlin.comparisons.compareValues

val Int.isPowerOf2: Boolean
    get() = this and (this - 1) == 0

private val MaximumPowerOf2 = 1 shl 30

fun Int.ceilingPowerOf2(): Int {
    var n = this - 1
    n = n or (n ushr 1)
    n = n or (n ushr 2)
    n = n or (n ushr 4)
    n = n or (n ushr 8)
    n = n or (n ushr 16)
    return if (n < 0) 1 else if (n >= MaximumPowerOf2) MaximumPowerOf2 else n + 1
}

val Int.abs: Int
    get() = Math.abs(this)

val Int.isPositive: Boolean
    get() = this > 0
val Int.isNegative: Boolean
    get() = this < 0

val Long.isPositive: Boolean
    get() = this > 0
val Long.isNegative: Boolean
    get() = this < 0

val Double.ceil: Double
    get() = Math.ceil(this)
val Double.floor: Double
    get() = Math.floor(this)

fun <T : Comparable<*>> max(o1: T, o2: T): T = if (compareValues(o1, o2) < 0) o2 else o1
fun <T : Comparable<*>> min(o1: T, o2: T): T = if (compareValues(o1, o2) > 0) o2 else o1