package com.frost.common.concurrent

import com.frost.common.Order
import com.frost.common.Ordered
import com.frost.common.lang.insurePowerOf2
import com.frost.common.logging.getLogger
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

val cpuNum = Runtime.getRuntime().availableProcessors()

@Component
class ExecutorContext {
    val logger by getLogger()

    private val index = AtomicInteger()
    private val nThread = (cpuNum * 2).insurePowerOf2()
    private val executors = (1..nThread).map {
        ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, PriorityBlockingQueue<Runnable>(16, Comparator<Runnable> { a, b ->
            val i1 = if (a is Ordered) a.order() else Order.NORMAL
            val i2 = if (b is Ordered) b.order() else Order.NORMAL
            i1.compareTo(i2)
        }), NamedThreadFactory("executor-context"))
    }

    @PostConstruct
    private fun init() {
        logger.info("${ExecutorContext::class.java.simpleName} pool size: $nThread")
    }

    private fun next(): ExecutorService = executors[index.andIncrement and executors.size - 1]
    private fun next(identity: Any): ExecutorService = executors[identity.hashCode() and executors.size - 1]

    fun submit(task: Runnable) = next().submit(task)
    fun submit(task: () -> Unit) = next().submit(task)
    fun <T> submit(task: Callable<T>): Future<T> = next().submit(task)

    fun submitTo(identity: Any, task: Runnable) = next(identity).submit(task)
    fun submitTo(identity: Any, task: () -> Unit) = next(identity).submit(task)
    fun <T> submitTo(identity: Any, task: Callable<T>): Future<T> = next(identity).submit(task)

    @PreDestroy
    fun shutdown() {
        val tasks = executors.flatMap { it.shutdownAndAwaitTermination() }
        logger.error("{} tasks not executed.", tasks.size)
    }
}

fun ExecutorService.shutdownAndAwaitTermination(timeout: Long = 30, timeUnit: TimeUnit = TimeUnit.SECONDS): List<Runnable> {
    shutdown()
    return if (awaitTermination(timeout, timeUnit)) {
        emptyList()
    } else {
        val canceledTasks = shutdownNow()
        awaitTermination(timeout, timeUnit)
        canceledTasks
    }
}