package com.frost.entity.cache

import com.frost.common.reflect.genericTypes
import com.frost.common.reflect.safeSet
import com.frost.entity.EntitySetting
import com.frost.entity.db.PersistService
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
    @Autowired
    private lateinit var persistImpl: PersistService
    @Autowired
    private lateinit var setting: EntitySetting

    private var entityCaches = hashMapOf<Class<*>, Any>()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? = bean

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val clazz = it.genericTypes()[1]
                    val cache = entityCaches.computeIfAbsent(clazz, { ctx.getBean(EntityCacheImpl::class.java, clazz) })
                    it.safeSet(bean, cache)
                },
                { EntityCache::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }
}