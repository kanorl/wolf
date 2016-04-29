package com.frostwolf.common.lang

fun <T> Collection<T>.mkString(start: String, sep: String, end: String): String {
    var first = true
    val b = StringBuilder(start)
    for (e in this) {
        if (first) {
            b.append(e)
            first = false
        } else {
            b.append(e).append(sep)
        }
    }
    return b.append(end).toString()
}

fun <T> Collection<T>.mkString(sep: String): String = mkString("", sep, "")