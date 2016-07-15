package com.frostwolf.entity.cache

import com.frostwolf.common.reflect.safeSet
import com.frostwolf.common.reflect.typeArgs
import com.frostwolf.entity.EntitySetting
import com.frostwolf.entity.db.PersistService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import javax.annotation.PreDestroy

@Suppress("UNCHECKED_CAST")
@Component
open class EntityCacheManager : BeanPostProcessor {

    @Autowired
    private lateinit var ctx: ApplicationContext
    @Autowired
    private lateinit var persistImpl: PersistService
    @Autowired
    private lateinit var setting: EntitySetting

    private var entityCaches = hashMapOf<Class<*>, EntityCacheImpl<*, *>>()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? = bean

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        ReflectionUtils.doWithFields(bean.javaClass,
                {
                    val clazz = it.typeArgs()[1]
                    val cache = entityCaches.computeIfAbsent(clazz, { ctx.getBean(EntityCacheImpl::class.java, clazz) })
                    it.safeSet(bean, cache)
                },
                { EntityCache::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }

    @PreDestroy
    private fun destroy() {
        entityCaches.values.forEach { it.updateEdited() }
    }
}