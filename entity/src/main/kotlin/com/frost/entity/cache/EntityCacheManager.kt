package com.frost.entity.cache

import com.frost.common.reflect.genericTypes
import com.frost.common.reflect.safeSet
import com.frost.entity.AbstractEntity
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

@Suppress("UNCHECKED_CAST")
@Component
class EntityCacheManager : BeanPostProcessor {

    @Autowired
    private lateinit var ctx: ApplicationContext

    private var entityCaches = CacheBuilder.newBuilder().build(CacheLoader.from<Class<AbstractEntity<Comparable<Any>>>, EntityCache<Comparable<Any>, AbstractEntity<Comparable<Any>>>> { clazz -> EntityCacheImpl(clazz!!) })

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? = bean

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val clazz = it.genericTypes()[1] as Class<AbstractEntity<Comparable<Any>>>
                    it.safeSet(bean, entityCaches.getUnchecked(clazz))
                },
                { EntityCache::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }
}