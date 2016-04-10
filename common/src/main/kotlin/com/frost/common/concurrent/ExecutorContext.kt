package com.frost.common.concurrent

import com.frost.common.lang.ceilingPowerOf2
import com.frost.common.logging.getLogger
import com.frost.common.time.millis
import com.google.common.util.concurrent.MoreExecutors
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

val cpuNum = Runtime.getRuntime().availableProcessors()

object ExecutorContext {
    private val logger by getLogger()
    private val parallelism = (cpuNum * 2).ceilingPowerOf2()
    private val executor = ForkJoinPool(parallelism, ForkJoinPool.defaultForkJoinWorkerThreadFactory, Thread.UncaughtExceptionHandler { t, e -> logger.error(e.message, e) }, true)
    private val taskQueues = (1..parallelism).map { OrderedTaskQueue() }.toTypedArray()
    private val transferer = Executors.newSingleThreadExecutor(NamedThreadFactory(name = "task-transferer", daemon = true))

    init {
        transferer.submit {
            val parkNanos = 5.millis().nanos
            while (true) {
                var park = true
                for (queue in taskQueues) {
                    queue.poll()?.let {
                        executor.submit(it)
                        park = false
                    }
                }
                if (park) {
                    LockSupport.parkNanos(parkNanos)
                }
            }
        }
    }

    fun submit(owner: Any, task: Runnable) {
        taskQueues[owner.hashCode() and (taskQueues.size - 1)].offer(task)
    }

    fun submit(owner: Any, task: Callable<*>) {
        taskQueues[owner.hashCode() and (taskQueues.size - 1)].offer(task)
    }

    fun submit(owner: Any, task: () -> Unit) {
        taskQueues[owner.hashCode() and (taskQueues.size - 1)].offer(task)
    }

    fun submit(task: Callable<*>) = executor.submit(task)

    fun submit(task: Runnable) = executor.submit(task)

    fun submit(task: () -> Any) = executor.submit(task)
}

private class OrderedTaskQueue : AbstractQueue<Callable<*>>(), Queue<Callable<*>> {

    val queue = ConcurrentLinkedQueue<Callable<*>>()
    val available = AtomicBoolean(true)

    override val size: Int = queue.size

    override fun iterator(): MutableIterator<Callable<*>> = queue.iterator()

    override fun poll(): Callable<*>? {
        if (!available.compareAndSet(true, false)) {
            return null
        }
        val task = queue.poll()
        task ?: setAvailable()
        return task
    }

    override fun peek(): Callable<*>? {
        throw UnsupportedOperationException()
    }

    override fun offer(e: Callable<*>): Boolean = queue.offer(Callable<kotlin.Any> {
        try {
            e.call()
        } finally {
            setAvailable()
        }
    })

    fun offer(e: Runnable) = queue.offer(Callable<kotlin.Any> {
        try {
            e.run()
        } finally {
            setAvailable()
        }
    })

    fun setAvailable() = available.set(true);
}

fun ExecutorService.shutdownAndAwaitTermination(timeout: Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS): Boolean = MoreExecutors.shutdownAndAwaitTermination(this, timeout, timeUnit)