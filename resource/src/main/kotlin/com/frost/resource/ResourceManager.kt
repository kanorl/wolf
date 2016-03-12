package com.frost.resource

import com.frost.common.logging.getLogger
import com.frost.common.reflect.genericType
import com.frost.common.reflect.subTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import org.springframework.validation.DataBinder
import org.springframework.validation.Validator
import java.util.*
import javax.annotation.PostConstruct

@Component
class ResourceManager : BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    val logger by getLogger()

    @Autowired
    private lateinit var reader: Reader
    @Autowired
    private lateinit var validator: Validator
    @Value("\${resource.path}")
    private lateinit var baseDir: String
    private var containers = mapOf<Class<out Resource>, ContainerImpl<Resource>>()
    private var injectedContainers = mapOf<Class<out Resource>, DelegatingContainer<Resource>>()

    @PostConstruct
    private fun init() {
        Resource::class.java.subTypes().forEach { containers += (it to ContainerImpl(reader.read(it, baseDir))) }
        validate(containers)
    }

    private fun validate(containers: Map<Class<out Resource>, ContainerImpl<out Resource>>) {
        val resources = containers.mapValues { it.value.sorted }
        resources.forEach {
            val duplicate = it.value.groupBy { it.getId() }.filter { it.value.size != 1 }.keys
            if (!duplicate.isEmpty()) throw DuplicateResourceException(it.key, duplicate)
        }

        val errors = resources.values.flatten().flatMap {
            val bindingResult = DataBinder(it, "${it.javaClass.simpleName}[${it.getId()}]").bindingResult
            validator.validate(it, bindingResult)
            bindingResult.allErrors
        }
        if (errors.isNotEmpty()) throw ValidateFailedException(errors)
    }

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? = bean

    @Suppress("UNCHECKED_CAST")
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val clazz = it.genericType() as Class<out Resource>
                    val container = DelegatingContainer<Resource>()
                    injectedContainers += (clazz to container)
                    ReflectionUtils.makeAccessible(it)
                    it.set(bean, container)
                },
                { it.type == Container::class.java }
        )
        return bean
    }

    @Synchronized
    fun reload(vararg classes: Class<out Resource>) {
        val prev = containers
        var reloaded: Map<Class<out Resource>, ContainerImpl<out Resource>>
        try {
            reloaded = classes.associate { (it to  ContainerImpl(reader.read(it, baseDir))) }
            val current = HashMap(prev) + reloaded
            this.containers = current

            validate(reloaded)
            refresh(reloaded)
        } catch(e: Exception) {
            this.containers = prev
            logger.error("Rollback from resource reload${classes.map { it.simpleName }}: ${e.message}", e)
        }
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        refresh(containers)
    }

    @Synchronized
    private fun refresh(containers: Map<Class<out Resource>, ContainerImpl<Resource>>) {
        val prev = containers.keys.filter { injectedContainers.containsKey(it) }.associate { (it to injectedContainers[it]!!.delegatee) }
        try {
            containers.forEach { injectedContainers[it.key]?.delegatee = it.value }
        } catch(e: Exception) {
            prev.forEach { injectedContainers[it.key]!!.delegatee = it.value }
            logger.error("Rollback from resource refresh: ${e.message}", e)
        }
    }

    internal fun container(clazz: Class<*>): Container<*>? = containers[clazz]
}