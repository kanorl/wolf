package com.frost.common.lang

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

fun Int.abs(): Int = Math.abs(this)

val Int.isPositive: Boolean
    get() = this > 0
val Int.isNegative: Boolean
    get() = this < 0

val Long.isPositive: Boolean
    get() = this > 0
val Long.isNegative: Boolean
    get() = this < 0