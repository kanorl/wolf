package com.frostwolf.resource

import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.reflect.subTypes
import com.frostwolf.common.toObj
import com.google.common.io.Files
import org.springframework.beans.factory.FactoryBean
import org.springframework.stereotype.Component
import java.io.File
import javax.inject.Inject

@Component
class ResourceReaderFactoryBean : FactoryBean<Reader> {
    val logger by getLogger()

    @Inject
    private lateinit var setting: ResourceSetting

    override fun getObject(): Reader {
        val reader = Reader::class.java.subTypes().map { it.kotlin.objectInstance }.find { it?.name.equals(setting.reader) }
        reader?.let { logger.info("Using Reader: {}", reader.javaClass.simpleName) }
        return reader ?: throw NoSuchReaderException("Reader[${setting.reader}] not found in ${Reader::class.java.subTypes().map { it.kotlin.objectInstance?.name }.firstOrNull()}")
    }

    override fun isSingleton(): Boolean = true

    override fun getObjectType(): Class<*>? = Reader::class.java
}

class NoSuchReaderException(msg: String) : RuntimeException(msg)

interface Reader {
    val name: String
    fun <T : Resource> read(type: Class<T>, baseDir: String): List<T>
}

object JsonReader : Reader {
    val logger by getLogger()

    override val name: String = "json"

    override fun <T : Resource> read(type: Class<T>, baseDir: String): List<T> {
        val file = File(baseDir + File.separator + type.simpleName + ".json")
        return if (file.exists()) {
            Files.toString(file, Charsets.UTF_8).toObj<List<T>>()
        } else {
            logger.error("Resource[${type.simpleName}] file[$file] not found.")
            listOf()
        }
    }
}