package com.frostwolf.common.scheduling

import com.frostwolf.common.Identified
import com.frostwolf.common.Named
import com.frostwolf.common.concurrent.ExecutorContext
import com.frostwolf.common.concurrent.MillisBasedScheduledThreadPoolExecutor
import com.frostwolf.common.concurrent.NamedThreadFactory
import com.frostwolf.common.concurrent.RejectedExecutionHandler2
import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.time.FiniteDuration
import com.frostwolf.common.time.toDate
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
internal open class MillisBasedTaskScheduler : ThreadPoolTaskScheduler() {
    val log by getLogger()

    @Autowired(required = false)
    private var handler = RejectedExecutionHandler2 { runnable, executor ->
        log.error("Task[{}] rejected from[{}]", runnable, executor)
    }

    override fun createExecutor(poolSize: Int, threadFactory: ThreadFactory?, rejectedExecutionHandler: RejectedExecutionHandler?): ScheduledExecutorService? {
        return MillisBasedScheduledThreadPoolExecutor(1, 30000, NamedThreadFactory("scheduler"), handler)
    }
}

interface Scheduler {
    fun schedule(cron: String, task: () -> Unit): ScheduledFuture<*>
    fun schedule(cron: String, task: Runnable): ScheduledFuture<*>

    fun scheduleOnce(startTime: LocalDateTime, task: () -> Unit): ScheduledFuture<*>
    fun scheduleOnce(startTime: LocalDateTime, task: Runnable): ScheduledFuture<*>

    fun scheduleOnce(delay: FiniteDuration, task: () -> Unit): ScheduledFuture<*>
    fun scheduleOnce(delay: FiniteDuration, task: Runnable): ScheduledFuture<*>

    fun scheduleAtFixedRate(period: FiniteDuration, task: () -> Unit): ScheduledFuture<*>
    fun scheduleAtFixedRate(period: FiniteDuration, task: Runnable): ScheduledFuture<*>

    fun scheduleAtFixedRate(startTime: LocalDateTime, period: FiniteDuration, task: () -> Unit): ScheduledFuture<*>
    fun scheduleAtFixedRate(startTime: LocalDateTime, period: FiniteDuration, task: Runnable): ScheduledFuture<*>

    fun scheduleWithFixedDelay(delay: FiniteDuration, task: () -> Unit): ScheduledFuture<*>
    fun scheduleWithFixedDelay(delay: FiniteDuration, task: Runnable): ScheduledFuture<*>

    fun scheduleWithFixedDelay(startTime: LocalDateTime, delay: FiniteDuration, task: () -> Unit): ScheduledFuture<*>
    fun scheduleWithFixedDelay(startTime: LocalDateTime, delay: FiniteDuration, task: Runnable): ScheduledFuture<*>
}

@Component
internal open class DelegatedScheduler : Scheduler {
    @Autowired
    private lateinit var delegate: MillisBasedTaskScheduler

    override fun schedule(cron: String, task: () -> Unit): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(task), CronTrigger(cron))

    override fun schedule(cron: String, task: Runnable): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(task), CronTrigger(cron))

    override fun scheduleOnce(startTime: LocalDateTime, task: () -> Unit): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(task), startTime.toDate())

    override fun scheduleOnce(startTime: LocalDateTime, task: Runnable): ScheduledFuture<*> =
            delegate.schedule(LoggingRunnable(task), startTime.toDate())

    override fun scheduleOnce(delay: FiniteDuration, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduledExecutor.schedule(LoggingRunnable(task), delay.value, delay.timeUnit)

    override fun scheduleOnce(delay: FiniteDuration, task: Runnable): ScheduledFuture<*> =
            delegate.scheduledExecutor.schedule(LoggingRunnable(task), delay.value, delay.timeUnit)

    override fun scheduleAtFixedRate(period: FiniteDuration, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduledExecutor.scheduleAtFixedRate(LoggingRunnable(task), 0, period.value, period.timeUnit)

    override fun scheduleAtFixedRate(period: FiniteDuration, task: Runnable): ScheduledFuture<*> =
            delegate.scheduledExecutor.scheduleAtFixedRate(LoggingRunnable(task), 0, period.value, period.timeUnit)

    override fun scheduleAtFixedRate(startTime: LocalDateTime, period: FiniteDuration, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduleAtFixedRate(LoggingRunnable(task), startTime.toDate(), period.millis)

    override fun scheduleAtFixedRate(startTime: LocalDateTime, period: FiniteDuration, task: Runnable): ScheduledFuture<*> =
            delegate.scheduleAtFixedRate(LoggingRunnable(task), startTime.toDate(), period.millis)

    override fun scheduleWithFixedDelay(delay: FiniteDuration, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduledExecutor.scheduleWithFixedDelay(LoggingRunnable(task), 0, delay.value, delay.timeUnit)

    override fun scheduleWithFixedDelay(delay: FiniteDuration, task: Runnable): ScheduledFuture<*> =
            delegate.scheduledExecutor.scheduleWithFixedDelay(LoggingRunnable(task), 0, delay.value, delay.timeUnit)

    override fun scheduleWithFixedDelay(startTime: LocalDateTime, delay: FiniteDuration, task: () -> Unit): ScheduledFuture<*> =
            delegate.scheduleWithFixedDelay(LoggingRunnable(task), startTime.toDate(), delay.millis)

    override fun scheduleWithFixedDelay(startTime: LocalDateTime, delay: FiniteDuration, task: Runnable): ScheduledFuture<*> =
            delegate.scheduleWithFixedDelay(LoggingRunnable(task), startTime.toDate(), delay.millis)
}

private class LoggingRunnable(val task: Runnable) : Runnable {
    constructor(task: () -> Any) : this(Runnable { task() })

    companion object {
        val logger by getLogger()
    }

    val action = {
        val name = if (task is Named) task.name else task.javaClass.simpleName
        logger.info("开始执行[{}]", name)
        try {
            task.run()
        } catch(e: Exception) {
            logger.error("执行失败[$name]", e)
        }
        logger.info("完成执行[{}]", name)
    }

    override fun run() {
        if (task is Identified<*>) {
            ExecutorContext.submit(task.id, action)
        } else {
            ExecutorContext.submit(action)
        }
    }
}