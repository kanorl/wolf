package com.frost.common

import java.util.concurrent.ThreadLocalRandom

fun current() = ThreadLocalRandom.current()

fun nextInt(bound: Int): Int = current().nextInt(bound)

fun nextInt(origin: Int, bound: Int): Int = current().nextInt(origin, bound)

fun between(origin: Int, bound: Int): Int = nextInt(origin, bound + 1)

fun nextBoolean(): Boolean = current().nextBoolean()

fun <T> random(c: Collection<T>, totalWeight: Int? = null, weigher: (T) -> Int): T? {
    if (c.isEmpty()) return null
    val total = totalWeight ?: c.sumBy { t -> weigher(t) }
    if (total <= 0) return null
    var random = nextInt(total)
    for (t in c) {
        val weigh = weigher(t)
        if (weigh > random) {
            return t
        }
        random -= weigh
    }
    return c.elementAt(nextInt(c.size))
}