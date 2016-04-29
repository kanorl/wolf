package com.frostwolf.common

data class Bytes(val value: Int) {
    companion object {
        fun parse(str: String): Int = Math.toIntExact(
                when {
                    str.endsWith('b', true) -> str.dropLast(1).toLong()
                    str.endsWith('k', true) -> str.dropLast(1).toLong() * 1024
                    str.endsWith('m', true) -> str.dropLast(1).toLong() * 1024 * 1024
                    str.endsWith('g', true) -> str.dropLast(1).toLong() * 1024 * 1024 * 1024
                    else -> str.toLong()
                }
        )
    }

    constructor(str: String) : this(parse(str))

    operator fun compareTo(other: Bytes): Int = this.value.compareTo(other.value)
    operator fun compareTo(other: Int): Int = this.value.compareTo(other)
}

operator fun Int.compareTo(other: Bytes): Int = this.compareTo(other.value)