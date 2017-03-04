package com.frostwolf.common

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.frostwolf.common.logging.defaultLogger

val jacksonObjectMapper: ObjectMapper = run {
    val mapper = jacksonObjectMapper()
    mapper.findAndRegisterModules().
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
fun <T : Any> T.toJson(pretty: Boolean = false): String = if (pretty) jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this) else jacksonObjectMapper.writeValueAsString(this)

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

fun <T : Any> String.toObj(type: TypeReference<T>): T = jacksonObjectMapper.readValue(this, type)

fun <T : Any> String.toObj(type: Class<T>): T = jacksonObjectMapper.readValue(this, type)

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