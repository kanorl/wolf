package com.frost.common.concurrent

import com.frost.common.lang.ceilingPowerOf2
import com.frost.common.lang.isPowerOf2
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
    val parallelism: Int = (cpuNum * 2).ceilingPowerOf2()
    private var taskPools = arrayOf<TaskPool>()
    val defaultTaskPool = createTaskPool("default")
    private val executor = ForkJoinPool(parallelism, ForkJoinPool.defaultForkJoinWorkerThreadFactory, Thread.UncaughtExceptionHandler { t, e -> logger.error(e.message, e) }, true)
    private val transferer = Executors.newSingleThreadExecutor(NamedThreadFactory(name = "task-transferer", daemon = true))

    init {
        transferer.submit {
            val parkNanos = 5.millis().nanos
            while (true) {
                var park = true
                for (taskPool in taskPools) {
                    for (queue in taskPool.taskQueues) {
                        queue.poll()?.let {
                            executor.submit(it)
                            park = false
                        }
                    }
                }
                if (park) {
                    LockSupport.parkNanos(parkNanos)
                }
            }
        }
    }

    @Synchronized
    fun createTaskPool(name: String, parallelism: Int = this.parallelism): TaskPool {
        taskPools.find { it.name == name }?.let { throw IllegalStateException("TaskPool[$name] already exists") }
        val p = TaskPool(name, parallelism)
        taskPools += p
        logger.info("TaskPool[{}] Created.", name)
        return p
    }

    fun submit(owner: Any, task: Runnable) = defaultTaskPool.submit(owner, task)

    fun submit(owner: Any, task: Callable<*>) = defaultTaskPool.submit(owner, task)

    fun submit(owner: Any, task: () -> Unit) = defaultTaskPool.submit(owner, task)

    fun submit(task: Callable<*>) = executor.submit(task)

    fun submit(task: Runnable) = executor.submit(task)

    fun submit(task: () -> Any) = executor.submit(task)
}

class TaskPool(val name: String, val parallelism: Int = (cpuNum * 2).ceilingPowerOf2()) {
    internal val taskQueues = (1..parallelism).map { OrderedTaskQueue() }.toTypedArray()

    init {
        check(parallelism.isPowerOf2, { "`parallelism` must be power of 2" })
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

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false;
        }
        if (other !is TaskPool) {
            return false
        }
        return this.name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

internal class OrderedTaskQueue : AbstractQueue<Callable<*>>(), Queue<Callable<*>> {

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