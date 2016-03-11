package com.frost.resource

import com.frost.common.reflect.subTypes
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

abstract class Reader {

    @Value("\${resource.path}")
    protected lateinit var baseDir: String

    fun <T : Resource> read(type: Class<T>): List<T> {
        val resources = read(type, baseDir)
        resources.forEach { it.afterPropertiesSet() }
        return resources
    }

    protected abstract fun <T : Resource> read(type: Class<T>, baseDir: String): List<T>
}

internal class EmptyReader : Reader() {
    override fun <T : Resource> read(type: Class<T>, baseDir: String): List<T> = emptyList()
}

@Component
class ConfigurationReaderFactoryBean : FactoryBean<Reader> {

    @Value("\${resource.reader}")
    private var readerName: String = "empty"
    @Autowired
    private lateinit var ctx: ApplicationContext

    override fun getObjectType(): Class<*>? = Reader::class.java

    override fun isSingleton(): Boolean = true

    override fun getObject(): Reader? = try {
        ctx.autowireCapableBeanFactory.createBean(Reader::class.java.subTypes().first { c -> c.simpleName.startsWith(readerName, true) })
    } catch(e: Exception) {
        throw IllegalStateException("[$readerName] Reader not found.")
    }
}