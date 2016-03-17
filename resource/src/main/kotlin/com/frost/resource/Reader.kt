package com.frost.resource

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

interface Reader {

    fun <T : Resource> read(type: Class<T>, path: String): List<T> {
        val resources = read0(type, path)
        resources.forEach { it.afterPropertiesSet() }
        return resources
    }

    fun <T : Resource> read0(type: Class<T>, path: String): List<T>
}

@Component
@ConditionalOnMissingBean(Reader::class)
class EmptyReader : Reader {
    override fun <T : Resource> read0(type: Class<T>, path: String): List<T> = emptyList()
}