package com.frostwolf.common.event

import com.frostwolf.common.Identified
import com.frostwolf.common.concurrent.ExecutorContext
import com.frostwolf.common.reflect.safeGet
import com.frostwolf.common.reflect.subTypes
import com.frostwolf.common.reflect.typeArgs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

interface Event

@Suppress("UNCHECKED_CAST")
@Component
class EventListenerManager : BeanPostProcessor {

    private var listeners: Map<Class<*>, List<EventListener<in Event>>> = mapOf()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean;
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val listener = it.safeGet<EventListener<in Event>>(bean)!!
                    it.typeArgs().first().subTypes(true).forEach {
                        listeners += (it to listeners.getOrElse(it, { listOf() }) + listener)
                    }
                },
                { com.frostwolf.common.event.EventListener::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }

    fun getListeners(clazz: Class<*>): List<EventListener<in Event>> {
        return listeners.getOrElse(clazz, { listOf() })
    }
}

@Component
class EventBus {

    @Autowired
    private lateinit var listenerManager: EventListenerManager

    fun post(event: Event) {
        listenerManager.getListeners(event.javaClass).forEach { listener ->
            if (event is Identified<*>) {
                ExecutorContext.submit(event.id) { listener.onEvent(event) }
            } else {
                ExecutorContext.submit { listener.onEvent(event) }
            }
        }
    }

    fun postSync(event: Event) {
        listenerManager.getListeners(event.javaClass).forEach { listener -> listener.onEvent(event) }
    }
}