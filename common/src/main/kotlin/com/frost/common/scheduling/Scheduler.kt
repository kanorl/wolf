package com.frost.common.scheduling

import com.frost.common.concurrent.*
import com.frost.common.logging.getLogger
import com.frost.common.logging.loggingFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory

@Component
internal class MillisBasedTaskScheduler : ThreadPoolTaskScheduler() {
    val log by getLogger()

    val handler = RejectedExecutionHandler2 { runnable, executor ->
        if (!executor.isShutdown) {
            log.error("Task[{}] rejected from[{}]", runnable, executor)
        }
    }

    override fun createExecutor(poolSize: Int, threadFactory: ThreadFactory?, rejectedExecutionHandler: RejectedExecutionHandler?): ScheduledExecutorService? {
        return MillisBasedScheduledThreadPoolExecutor(cpuNum, 30000, NamedThreadFactory("scheduler"), handler)
    }
}

interface Scheduler : TaskScheduler {
    fun schedule(task: Runnable, cron: String): ScheduledFuture<*>
}

@Component
class DelegatingScheduler : Scheduler {
    @Autowired
    private lateinit var delegate: MillisBasedTaskScheduler

    override fun schedule(task: Runnable, cron: String): ScheduledFuture<*> = schedule(task, CronTrigger(cron))

    override fun schedule(task: Runnable, trigger: Trigger): ScheduledFuture<*> {
        return delegate.schedule(LoggingRunnable(task), trigger)
    }

    override fun schedule(task: Runnable, startTime: Date): ScheduledFuture<*> {
        return delegate.schedule(LoggingRunnable(task), startTime)
    }

    override fun scheduleAtFixedRate(task: Runnable, startTime: Date, period: Long): ScheduledFuture<*> {
        return delegate.scheduleAtFixedRate(LoggingRunnable(task), startTime, period)
    }

    override fun scheduleAtFixedRate(task: Runnable, period: Long): ScheduledFuture<*> {
        return delegate.scheduleAtFixedRate(LoggingRunnable(task), period)
    }

    override fun scheduleWithFixedDelay(task: Runnable, startTime: Date, delay: Long): ScheduledFuture<*> {
        return delegate.scheduleWithFixedDelay(LoggingRunnable(task), startTime, delay)
    }

    override fun scheduleWithFixedDelay(task: Runnable, delay: Long): ScheduledFuture<*> {
        return delegate.scheduleWithFixedDelay(LoggingRunnable(task), delay)
    }
}

class LoggingRunnable(val delegate: Runnable) : Runnable {
    companion object {
        val logger by getLogger()
    }

    override fun run() {
        val name = if (delegate is NamedTask) delegate.name else delegate.javaClass.name
        logger.info("开始执行[{}]", name)
        try {
            delegate.run()
        } catch(e: Exception) {
            logger.error("执行失败[{}]".loggingFormat(name), e)
        }
        logger.info("完成执行[{}]", name)
    }
}