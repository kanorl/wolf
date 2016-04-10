package com.frost.common.time

import com.frost.common.lang.isNegative
import java.util.concurrent.TimeUnit

data class FiniteDuration(val duration: Long, val timeUnit: TimeUnit) {
    init {
        check(!duration.isNegative, { "`duration` must not be a negative number" })
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

    val millis = timeUnit.toMillis(duration)
    val seconds = timeUnit.toSeconds(duration)
    val minutes = timeUnit.toMinutes(duration)
    val days = timeUnit.toDays(duration)
    val nanos = timeUnit.toNanos(duration)
    fun sleep() = timeUnit.sleep(duration)
}

fun Int.millis(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.MILLISECONDS)
fun Int.seconds(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.SECONDS)
fun Int.minutes(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.MINUTES)
fun Int.days(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.DAYS)

fun String.toDuration(): FiniteDuration = FiniteDuration(this)

fun sleep(duration: FiniteDuration) = duration.timeUnit.sleep(duration.duration)