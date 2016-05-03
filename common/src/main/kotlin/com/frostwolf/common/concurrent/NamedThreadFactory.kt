package com.frostwolf.common.concurrent

import com.frostwolf.common.Named
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(override val name: String, val daemon: Boolean = false, val priority: Int = Thread.currentThread().priority) : ThreadFactory, Named {
    val counter = AtomicInteger();
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, name + "-" + counter.incrementAndGet())
        thread.priority = priority
        thread.isDaemon = daemon
        return thread
    }
}