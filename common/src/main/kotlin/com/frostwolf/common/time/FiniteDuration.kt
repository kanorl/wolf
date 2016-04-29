package com.frostwolf.common.time

import com.frostwolf.common.lang.isNegative
import java.util.concurrent.TimeUnit

data class FiniteDuration(val value: Long, val timeUnit: TimeUnit) : Comparable<FiniteDuration> {
    override fun compareTo(other: FiniteDuration): Int = this.millis.compareTo(other.millis)

    init {
        check(!value.isNegative, { "`duration` must not be a negative number" })
    }

    companion object {
        val zero = 0.millis()
        fun parse(string: String): Pair<Long, TimeUnit> = when {
            string.endsWith("ms") -> (string.substring(0, string.length - 2).toLong() to TimeUnit.MILLISECONDS)
            string.endsWith("s") -> (string.substring(0, string.length - 1).toLong() to TimeUnit.SECONDS)
            string.endsWith("m") -> (string.substring(0, string.length - 1).toLong() to TimeUnit.MINUTES)
            string.endsWith("h") -> (string.substring(0, string.length - 1).toLong() to TimeUnit.HOURS)
            string.endsWith("d") -> (string.substring(0, string.length - 2).toLong() to TimeUnit.DAYS)
            else -> throw UnsupportedOperationException()
        }
    }

    constructor(pair: Pair<Long, TimeUnit>) : this(pair.first, pair.second)

    constructor(string: String) : this(parse(string))

    val millis = timeUnit.toMillis(value)
    val seconds = timeUnit.toSeconds(value)
    val minutes = timeUnit.toMinutes(value)
    val days = timeUnit.toDays(value)
    val nanos = timeUnit.toNanos(value)
    fun sleep() = timeUnit.sleep(value)

}

fun Int.millis(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.MILLISECONDS)
fun Int.seconds(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.SECONDS)
fun Int.minutes(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.MINUTES)
fun Int.days(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.DAYS)

fun String.toDuration(): FiniteDuration = FiniteDuration(this)