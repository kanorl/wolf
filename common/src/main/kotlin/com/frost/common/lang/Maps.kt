package com.frost.common.lang

fun <K, V> Map<K, V>.mkString(start: String, kvSep: String, elementSep: String, end: String): String {
    var first = true
    val b = StringBuilder(start)
    for ((k, v) in this) {
        if (first) {
            b.append(k).append(kvSep).append(v)
            first = false
        } else {
            b.append(elementSep)
            b.append(k).append(kvSep).append(v)
        }
    }
    return b.append(end).toString()
}

fun <K, V> Map<K, V>.mkString(kvSep: String = "=", elementSep: String = ","): String = this.mkString("", kvSep, elementSep, "")