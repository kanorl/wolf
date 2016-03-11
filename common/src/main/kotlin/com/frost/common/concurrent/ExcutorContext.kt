package com.frost.common.concurrent

import com.frost.common.Order
import com.frost.common.Ordered
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy

class Executor(name: String) : ThreadPoolExecutor(cpuNum, Runtime.getRuntime().availableProcessors(), 0, TimeUnit.NANOSECONDS, PriorityBlockingQueue<Runnable>(16, Comparator<Runnable> { a, b ->
    val i1 = if (a is Ordered) a.order() else Order.NORMAL
    val i2 = if (b is Ordered) b.order() else Order.NORMAL
    i1.compareTo(i2)
}), NamedThreadFactory(name))

@Component
class CommonExecutorService(val executor: Executor = Executor("common-executor-service")) : ExecutorService by executor {
    @PreDestroy
    private fun onDestroy() {
        shutdownAndAwaitTermination()
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

val cpuNum = Runtime.getRuntime().availableProcessors()