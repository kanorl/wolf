package com.frost.common

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.frost.common.logging.defaultLogger

val jacksonObjectMapper = run {
    val mapper = jacksonObjectMapper()
    mapper.
            registerModule(JavaTimeModule()).
            registerModule(Jdk8Module()).
            registerModule(GuavaModule()).
            configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true).
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
            setSerializationInclusion(JsonInclude.Include.NON_NULL).
            setVisibility(
                    mapper.visibilityChecker.
                            withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                            withGetterVisibility(JsonAutoDetect.Visibility.NONE).
                            withSetterVisibility(JsonAutoDetect.Visibility.NONE).
                            withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
            )
}

/**
 * serialize T as String
 */
fun <T : Any> T.toJson(): String = jacksonObjectMapper.writeValueAsString(this)

/**
 * serialize T as ByteArray
 */
fun <T : Any> T.toJsonBytes(): ByteArray = jacksonObjectMapper.writeValueAsBytes(this)

/**
 * deserialize Json String as T
 *
 * @return T
 * @throws Exception if deserialize failed
 */
inline fun <reified T : Any> String.toObj(): T = jacksonObjectMapper.readValue(this, object : TypeReference<T>() {})

/**
 * deserialize Json String as T
 *
 * @return T or null if exception occurred
 */
inline fun <reified T : Any> String.toObjOrNull(): T? = try {
    jacksonObjectMapper.readValue(this, object : TypeReference<T>() {})
} catch(e: Exception) {
    defaultLogger.error(e.message, e)
    null
}