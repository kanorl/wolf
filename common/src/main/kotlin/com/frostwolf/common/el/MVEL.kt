@file:Suppress("UNCHECKED_CAST")

package com.frostwolf.common.el

import com.frostwolf.common.logging.defaultLogger
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.mvel2.MVEL
import java.io.Serializable

private val cache = CacheBuilder.newBuilder().maximumSize(500).build(CacheLoader.from<String, Serializable> { MVEL.compileExpression(it) })

fun <T> eval(expression: String, vararg params: Any): T {
    val args: Map<String, Any> = when (params.size) {
        0 -> mapOf()
        1 -> mapOf("n1" to params[0])
        2 -> mapOf("n1" to params[0], "n2" to params[1])
        3 -> mapOf("n1" to params[0], "n2" to params[1], "n3" to params[2])
        4 -> mapOf("n1" to params[0], "n2" to params[1], "n3" to params[2], "n4" to params[3])
        5 -> mapOf("n1" to params[0], "n2" to params[1], "n3" to params[2], "n4" to params[3], "n5" to params[4])
        6 -> mapOf("n1" to params[0], "n2" to params[1], "n3" to params[2], "n4" to params[3], "n5" to params[4], "n6" to params[5])
        else -> params.mapIndexed { i, number -> "n" + (i + 1) to number }.toMap()
    }
    return eval(expression, args)
}

fun <T> eval(expression: String, args: Map<String, Any>): T = try {
    MVEL.executeExpression(cache.getUnchecked(expression), args) as T
} catch(e: Exception) {
    defaultLogger.error("Eval failed: expression=$expression, args=$args", e)
    throw e
}