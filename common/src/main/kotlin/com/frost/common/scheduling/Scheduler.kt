package com.frost.common.scheduling

import com.frost.common.Identified
import com.frost.common.concurrent.*
import com.frost.common.logging.getLogger
import com.frost.common.logging.loggingFormat
import com.frost.common.time.FiniteDuration
import com.frost.common.time.toDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory

@Component
internal class MillisBasedTaskScheduler : ThreadPoolTaskScheduler() {
    val log by getLogger()

    @Autowired(required = false)
    private var handler: RejectedExecutionHandler2 = RejectedExecutionHandler2 { runnable, executor ->
        log.error("Task[{}] rejected from[{}]", runnable, executor)
    }

    override fun createExecutor(poolSize: Int, threadFactory: ThreadFactory?, rejectedExecutionHandler: RejectedExecutionHandler?): ScheduledExecutorService? {
        return MillisBasedScheduledThreadPoolExecutor(1, 30000, NamedThreadFactory("scheduler"), handler)
    }
}

interface Scheduler {
    fun schedule(cron: String, task: NamedRunnable): ScheduledFuture<*>
    fun schedule(cron: String, taskName: String, task: () -> Unit): ScheduledFuture<*>
    fun scheduleOnce(startTime: LocalDateTime, taskName: String, task: () -> Unit): ScheduledFuture<*>
    fun scheduleOnce(delay: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*>
    fun scheduleAtFixedRate(period: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*>
    fun scheduleAtFixedRate(startTime: LocalDateTime, period: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*>
    fun scheduleWithFixedDelay(delay: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*>
    fun scheduleWithFixedDelay(startTime: LocalDateTime, delay: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*>
}

@Component
internal class DelegatingScheduler : Scheduler {
    @Autowired
    private lateinit var delegate: MillisBasedTaskScheduler
    @Autowired
    private lateinit var executorContext: ExecutorContext

    override fun schedule(cron: String, task: NamedRunnable): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(task, executorContext), CronTrigger(cron))

    override fun schedule(cron: String, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(toNamedTask(taskName, task), executorContext), CronTrigger(cron))

    override fun scheduleOnce(startTime: LocalDateTime, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(toNamedTask(taskName, task), executorContext), startTime.toDate())

    override fun scheduleOnce(delay: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduledExecutor.schedule(LoggingRunnable(toNamedTask(taskName, task), executorContext), delay.value, delay.timeUnit)

    override fun scheduleAtFixedRate(period: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduledExecutor.scheduleAtFixedRate(LoggingRunnable(toNamedTask(taskName, task), executorContext), 0, period.value, period.timeUnit)

    override fun scheduleAtFixedRate(startTime: LocalDateTime, period: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduleAtFixedRate(LoggingRunnable(toNamedTask(taskName, task), executorContext), startTime.toDate(), period.millis)

    override fun scheduleWithFixedDelay(delay: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduledExecutor.scheduleWithFixedDelay(LoggingRunnable(toNamedTask(taskName, task), executorContext), 0, delay.value, delay.timeUnit)

    override fun scheduleWithFixedDelay(startTime: LocalDateTime, delay: FiniteDuration, taskName: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduleWithFixedDelay(LoggingRunnable(toNamedTask(taskName, task), executorContext), startTime.toDate(), delay.millis)

    private fun toNamedTask(name: String, task: () -> Unit): NamedRunnable = namedTask(name, task)
}

private class LoggingRunnable(val delegate: Runnable, val executorContext: ExecutorContext) : Runnable {
    val logger by getLogger()

    val action = {
        val name = if (delegate is NamedRunnable) delegate.name else delegate.javaClass.name
        logger.info("开始执行[{}]", name)
        try {
            delegate.run()
        } catch(e: Exception) {
            logger.error("执行失败[{}]".loggingFormat(name), e)
        }
        logger.info("完成执行[{}]", name)
    }

    override fun run() {
        if (delegate is Identified<*>) {
            executorContext.submit(delegate.id, action)
        } else {
            executorContext.submit(action)
        }
    }
}