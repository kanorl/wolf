package com.frost.common.concurrent

import com.frost.common.Identified
import java.util.concurrent.Callable

interface NamedRunnable : Runnable {
     val name: String
}

inline fun namedTask(name: String, crossinline op: () -> Unit): NamedRunnable = object : NamedRunnable {
    override val name: String = name

    override fun run() = op()
}

interface IdentifiedRunnable : Runnable, Identified<Any>

interface IdentifiedCallable<V> : Callable<V>, Identified<Any>

fun <T> identifiedTask(id: Any, op: () -> T): IdentifiedCallable<T> = object : IdentifiedCallable<T> {
    override fun call() = op()

    override val id = id
}