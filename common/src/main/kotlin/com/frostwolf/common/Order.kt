package com.frostwolf.common

interface Ordered : Comparable<Ordered> {
    fun order(): Int = Order.Normal
    override fun compareTo(other: Ordered): Int {
        return this.order().compareTo(other.order())
    }
}

object Order {
    const val Normal = 0
    const val Highest = Int.MIN_VALUE
    const val Lowest = Int.MAX_VALUE
}