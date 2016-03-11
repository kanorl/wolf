package com.frost.common.concurrent

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(val name: String, val priority: Int = Thread.NORM_PRIORITY, val daemon: Boolean = false) : ThreadFactory {
    val counter = AtomicInteger();
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, name + "-" + counter.incrementAndGet())
        thread.priority = priority
        thread.isDaemon = daemon
        return thread
    }
}