package com.frostwolf.common.lang

import java.util.concurrent.ThreadLocalRandom

fun current(): ThreadLocalRandom = ThreadLocalRandom.current()

fun nextInt(bound: Int): Int = current().nextInt(bound)

fun nextInt(origin: Int, bound: Int): Int = current().nextInt(origin, bound)

fun between(origin: Int, bound: Int): Int = nextInt(origin, bound + 1)

fun nextBoolean(): Boolean = current().nextBoolean()

fun <E : Any> Collection<E>.random(totalWeight: Int? = null, randomByIndexOnFail: Boolean = true, weigher: (E) -> Int): E? {
    if (this.isEmpty()) return null
    val total = totalWeight ?: this.sumBy { t -> weigher(t) }
    if (total > 0) {
        var random = nextInt(total)
        for (t in this) {
            val weigh = weigher(t)
            if (weigh > random) {
                return t
            }
            random -= weigh
        }
    }
    return if (randomByIndexOnFail) this.elementAt(nextInt(this.size)) else null
}