package com.frost.resource

import com.frost.common.logging.getLogger
import com.frost.common.toObj
import com.google.common.io.Files
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import java.io.File

interface Reader {
    fun <T : Resource> read(type: Class<T>, baseDir: String): List<T>
}

@Component
@ConditionalOnMissingBean(Reader::class)
class JsonReader : Reader {
    val logger by getLogger()

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