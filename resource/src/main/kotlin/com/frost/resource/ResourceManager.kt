package com.frost.resource

import com.frost.common.event.Event
import com.frost.common.event.EventBus
import com.frost.common.logging.getLogger
import com.frost.common.reflect.genericType
import com.frost.common.reflect.subTypes
import com.frost.resource.validation.ResourceInvalidException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import org.springframework.validation.DataBinder
import org.springframework.validation.Validator
import javax.annotation.PostConstruct

@Component
class ResourceManager : BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    val logger by getLogger()

    @Autowired
    private lateinit var eventBus: EventBus
    @Autowired
    private lateinit var reader: Reader
    @Autowired
    private lateinit var validator: Validator
    @Autowired
    private lateinit var setting: ResourceSetting

    private var containers = mapOf<Class<out Resource>, ContainerImpl<Resource>>()
    private var injectedContainers = hashMapOf<Class<out Resource>, DelegatingContainer<Resource>>()

    @PostConstruct
    private fun init() {
        Resource::class.java.subTypes().forEach { containers += (it to ContainerImpl(reader.read(it, setting.path))) }
        validate(containers)
    }

    private fun validate(containers: Map<Class<out Resource>, ContainerImpl<out Resource>>) {
        val resources = containers.mapValues { it.value.set }
        val errors = resources.values.flatten().flatMap {
            val bindingResult = DataBinder(it, "${it.javaClass.simpleName}[${it.id}]").bindingResult
            validator.validate(it, bindingResult)
            bindingResult.allErrors
        }
        if (errors.isNotEmpty()) throw ResourceInvalidException(errors)
    }

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? = bean

    @Suppress("UNCHECKED_CAST")
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val clazz = it.genericType() as Class<out Resource>
                    val container = DelegatingContainer<Resource>()
                    injectedContainers.put(clazz, container)
                    ReflectionUtils.makeAccessible(it)
                    it.set(bean, container)
                },
                { it.type == Container::class.java }
        )
        return bean
    }

    @Suppress("UNCHECKED_CAST")
    fun reload(vararg classNames: String): Boolean {
        return try {
            val array = classNames.map { Class.forName(it) as  Class<out Resource> }.toTypedArray()
            reload(*array)
            true
        } catch(e: Exception) {
            logger.error(e.message, e)
            false
        }
    }

    @Synchronized
    fun reload(vararg classes: Class<out Resource>): Boolean {
        for (clazz in classes) {
            try {
                val reloaded = mapOf(clazz to ContainerImpl(reader.read(clazz, setting.path)))
                validate(reloaded)
                containers + reloaded
                logger.info("Resource reloaded: {}", clazz.simpleName)
            } catch(e: Exception) {
                logger.error("Resource reload failed: ${clazz.simpleName}", e)
                return false
            }
        }
        refresh(classes.associate { it to containers[it]!! })
        logger.info("Reload {} resources complete.", classes.size)
        return true
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        refresh(containers)
    }

    @Synchronized
    private fun refresh(containers: Map<Class<out Resource>, ContainerImpl<Resource>>) {
        containers.forEach {
            val delegatingContainer = injectedContainers.computeIfAbsent(it.key, { DelegatingContainer<Resource>() })
            delegatingContainer.delegatee = it.value
        }
        eventBus.post(ResourceRefreshed(containers.keys))
    }

    internal fun container(clazz: Class<*>): Container<*>? = containers[clazz]
}

data class ResourceRefreshed(val classes: Set<Class<out Resource>>) : Event