package com.frost.common.event

import com.frost.common.Ordered
import com.frost.common.reflect.genericType
import com.frost.common.reflect.subTypes
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ListMultimap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import java.util.concurrent.ExecutorService

interface Event : Ordered

@Suppress("UNCHECKED_CAST")
@Component
class EventListenerManager : BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private var listeners: ListMultimap<Class<*>, EventListener<in Event>> = ArrayListMultimap.create()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean;
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val listeners = ArrayListMultimap.create<Class<*>, EventListener<in Event>>()
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    it.isAccessible = true
                    val listener = it.get(bean) as EventListener<in Event>
                    it.genericType().subTypes(true).forEach { eventType -> listeners.put(eventType, listener) }
                },
                { EventListener::class.java.isAssignableFrom(it.type) }
        )
        this.listeners.putAll(listeners)
        return bean;
    }

    fun getListeners(clazz: Class<*>): List<EventListener<in Event>> {
        return listeners.get(clazz)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        listeners = ImmutableListMultimap.copyOf(listeners)
    }
}

@Component
class EventBus {

    @Autowired
    private lateinit var listenerManager: EventListenerManager
    @Autowired
    private lateinit var executor: ExecutorService

    fun post(event: Event) {
        listenerManager.getListeners(event.javaClass).forEach { listener ->
            executor.submit(object : Runnable, Ordered {
                override fun order(): Int {
                    return event.order()
                }

                override fun run() {
                    listener.onEvent(event)
                }
            })
        }
    }

    fun postSync(event: Event) {
        listenerManager.getListeners(event.javaClass).forEach { listener -> listener.onEvent(event) }
    }
}