package com.frost.common

interface Ordered : Comparable<Ordered> {
    fun order(): Int = Order.NORMAL
    override fun compareTo(other: Ordered): Int {
        return this.order().compareTo(other.order())
    }
}

object Order {
    const val NORMAL = 0
    const val HIGHEST = Integer.MIN_VALUE
    const val LOWEST = Integer.MAX_VALUE
}