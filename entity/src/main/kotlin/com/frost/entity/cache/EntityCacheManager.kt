package com.frost.entity.cache

import com.frost.common.reflect.genericTypes
import com.frost.common.reflect.safeSet
import com.frost.entity.Entity
import com.frost.entity.db.Repository
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

@Suppress("UNCHECKED_CAST")
@Component
class EntityCacheManager : BeanPostProcessor {
    private lateinit var repository: Repository

    private var entityCaches = CacheBuilder.newBuilder().build(CacheLoader.from<Class<Entity<Comparable<Any>>>, EntityCache<Comparable<Any>, Entity<Comparable<Any>>>> { clazz -> StdEntityCache(clazz!!, repository) })

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? = bean

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val clazz = it.genericTypes()[1] as Class<Entity<Comparable<Any>>>
                    it.safeSet(bean, entityCaches.getUnchecked(clazz))
                },
                { EntityCache::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }
}