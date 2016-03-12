package com.frost.resource

import com.frost.common.logging.getLogger
import com.frost.common.reflect.subTypes
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

interface Reader {

    val name: String

    fun <T : Resource> read(type: Class<T>, path: String): List<T> {
        val resources = read0(type, path)
        resources.forEach { it.afterPropertiesSet() }
        return resources
    }

    fun <T : Resource> read0(type: Class<T>, path: String): List<T>
}

internal object EmptyReader : Reader {
    override val name: String = "empty"

    override fun <T : Resource> read0(type: Class<T>, path: String): List<T> = emptyList()
}

@Component
class ConfigurationReaderFactoryBean : FactoryBean<Reader> {
    val logger by getLogger()

    @Value("\${resource.reader}")
    private var readerName: String = "empty"

    override fun getObjectType(): Class<*>? = Reader::class.java

    override fun isSingleton(): Boolean = true

    override fun getObject(): Reader? {
        val reader = Reader::class.java.subTypes().map { it.kotlin.objectInstance }.first { it?.name == readerName }
        reader ?: throw IllegalStateException("[$readerName] Reader not found in ${Reader::class.java.subTypes().map { it.kotlin.objectInstance?.name }}")
        logger.info("Resource reader is {}", reader.javaClass.simpleName)
        return reader
    }
}