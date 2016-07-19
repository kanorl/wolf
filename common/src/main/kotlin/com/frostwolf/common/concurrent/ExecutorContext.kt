package com.frostwolf.common.concurrent

import com.frostwolf.common.lang.ceilingPowerOf2
import com.frostwolf.common.lang.isPowerOf2
import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.time.millis
import com.google.common.util.concurrent.MoreExecutors
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

val cpuNum = Runtime.getRuntime().availableProcessors()

object ExecutorContext {
    private val logger by getLogger()
    private var taskPools = arrayOf<TaskPool>()
    private val defaultTaskPool = createTaskPool("default")
    private val executor = ForkJoinPool(cpuNum, ForkJoinPool.defaultForkJoinWorkerThreadFactory, Thread.UncaughtExceptionHandler { t, e -> logger.error(e.message, e) }, true)
    private val transferer = Executors.newSingleThreadExecutor(NamedThreadFactory("task-transferer", true))

    init {
        transferer.submit {
            val duration = 5.millis
            while (true) {
                var sleep = true
                val pools = taskPools
                pools.forEach {
                    it.taskQueues.forEach {
                        it.poll()?.let {
                            try {
                                executor.submit(it)
                            } catch(e: Exception) {
                                logger.error("Transferer failed to submit task to the executor", e)
                            }
                            sleep = false
                        }
                    }
                }
                if (sleep) duration.sleep()
            }
        }

        MoreExecutors.addDelayedShutdownHook(transferer, 1, TimeUnit.MINUTES)

        Runtime.getRuntime().addShutdownHook(thread(false) {
            if (!executor.awaitQuiescence(1, TimeUnit.MINUTES)) {
                logger.error("ExecutorContext waiting quiescence timeout")
            } else {
                logger.info("ExecutorContext terminated")
            }
        })
    }

    @Synchronized
    fun createTaskPool(name: String, parallelism: Int = cpuNum.ceilingPowerOf2()): TaskPool {
        taskPools.find { it.name == name }?.let { throw IllegalStateException("TaskPool[$name] already exists") }
        val p = TaskPool(name, parallelism)
        taskPools += p
        logger.info("TaskPool[{}] Created.", name)
        return p
    }

    fun submit(owner: Any, task: Runnable) = defaultTaskPool.submit(owner, task)

    fun submit(owner: Any, task: Callable<*>) = defaultTaskPool.submit(owner, task)

    fun submit(owner: Any, task: () -> Unit) = defaultTaskPool.submit(owner, task)

    fun submit(task: Callable<*>) = try {
        executor.submit(task)
    } catch(e: Exception) {
        logger.error("Failed to submit task to the executor", e)
    }

    fun submit(task: Runnable) = try {
        executor.submit(task)
    } catch(e: Exception) {
        logger.error("Failed to submit task to the executor", e)
    }

    fun submit(task: () -> Any) = try {
        executor.submit(task)
    } catch(e: Exception) {
        logger.error("Failed to submit task to the executor", e)
    }!!
}

class TaskPool(val name: String, parallelism: Int = (cpuNum * 2).ceilingPowerOf2()) {
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

    internal fun isEmpty(): Boolean {
        return taskQueues.all { it.isEmpty() }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
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

    private val queue = ConcurrentLinkedQueue<Callable<*>>()
    private val available = AtomicBoolean(true)

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

    fun setAvailable() = available.set(true)
}

fun ExecutorService.shutdownAndAwaitTermination(timeout: Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS): Boolean = MoreExecutors.shutdownAndAwaitTermination(this, timeout, timeUnit)