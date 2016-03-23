@file:Suppress("UNCHECKED_CAST")

package com.frost.io

import com.frost.common.lang.emptyByteArray
import com.google.protobuf.MessageLite
import io.protostuff.LinkedBuffer
import io.protostuff.ProtobufIOUtil
import io.protostuff.ProtostuffIOUtil
import io.protostuff.runtime.RuntimeSchema
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap

@Configuration
open class CodecConfiguration {

    @Bean
    @ConditionalOnMissingBean(Codec::class)
    open fun codec(): Codec = StringCodec
}

interface Codec {
    fun encode(obj: Any?): ByteArray
    fun <T> decode(data: ByteArray, type: Class<T>): T
}

object ProtoBufCodec : Codec {

    private val defaultInstances = ConcurrentHashMap<Class<out MessageLite>, MessageLite>()

    private fun <T : MessageLite> defaultInstance(type: Class<out T>): MessageLite = defaultInstances.computeIfAbsent(type, { it.getMethod("getDefaultInstance").invoke(null) as T }) as T

    override fun encode(obj: Any?): ByteArray = (obj as? MessageLite )?.toByteArray() ?: emptyByteArray

    override fun <T> decode(data: ByteArray, type: Class<T>): T {
        val t = type as? Class<out MessageLite> ?: throw IllegalArgumentException("'type' must be subtype of MessageLite")
        return defaultInstance(t).newBuilderForType().mergeFrom(data).build() as T
    }

    inline fun <reified T : MessageLite> decode(data: ByteArray): T = this.decode(data, T::class.java)
}

object StringCodec : Codec {
    override fun encode(obj: Any?): ByteArray = obj?.toString()?.toByteArray() ?: emptyByteArray

    override fun <T> decode(data: ByteArray, type: Class<T>): T = String(data) as T

    fun decode(data: ByteArray) = String(data)
}

object ProtostuffCodec : Codec {
    override fun encode(obj: Any?): ByteArray {
        obj ?: return emptyByteArray
        val schema = RuntimeSchema.getSchema(obj.javaClass)
        return ProtostuffIOUtil.toByteArray(obj, schema, LinkedBuffer.allocate(LinkedBuffer.MIN_BUFFER_SIZE))
    }

    override fun <T> decode(data: ByteArray, type: Class<T>): T {
        val schema = RuntimeSchema.getSchema(type)
        val message = schema.newMessage()
        ProtostuffIOUtil.mergeFrom(data, message, schema)
        return message
    }
}

object ProtostuffProtoBufCodec : Codec {
    override fun encode(obj: Any?): ByteArray {
        obj ?: return emptyByteArray
        val schema = RuntimeSchema.getSchema(obj.javaClass)
        return ProtobufIOUtil.toByteArray(obj, schema, LinkedBuffer.allocate(LinkedBuffer.MIN_BUFFER_SIZE))
    }

    override fun <T> decode(data: ByteArray, type: Class<T>): T {
        val schema = RuntimeSchema.getSchema(type)
        val message = schema.newMessage()
        ProtobufIOUtil.mergeFrom(data, message, schema)
        return message
    }
}