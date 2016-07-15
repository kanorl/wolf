package com.frostwolf.common.scheduling

import com.frostwolf.common.concurrent.task
import com.frostwolf.common.reflect.safeGet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Scheduled(val name: String, val cron: Cron)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cron(val value: String, val beanName: Boolean = false)

@Component
open class ScheduledManager : BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private lateinit var ctx: ApplicationContext
    @Autowired
    private lateinit var scheduler: Scheduler

    private var tasks = mapOf<Runnable, String>()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean;
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String?): Any? {
        ReflectionUtils.doWithFields(bean.javaClass, {
            val scheduled = it.getAnnotation(Scheduled::class.java)
            val name = scheduled.name
            val cron = if (scheduled.cron.beanName) ctx.getBean(scheduled.cron.value, String::class.java) else scheduled.cron.value
            val op = it.safeGet<Function0<*>>(bean)!!
            val task = task(name = name) { op.invoke() }
            tasks += (task to cron)
        }, { it.type == Function0::class.java && it.isAnnotationPresent(Scheduled::class.java) })

        return bean
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        tasks.forEach { scheduler.schedule(it.value, it.key) }
        tasks = emptyMap()
    }
}
