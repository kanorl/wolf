@file:Suppress("UNCHECKED_CAST")

package com.frost.io

import com.frost.common.lang.emptyByteArray
import com.frost.common.logging.getLogger
import com.frost.common.reflect.subTypes
import com.frost.io.netty.config.SocketSetting
import com.google.protobuf.MessageLite
import io.protostuff.LinkedBuffer
import io.protostuff.ProtobufIOUtil
import io.protostuff.ProtostuffIOUtil
import io.protostuff.runtime.RuntimeSchema
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
internal class CodecFactoryBean : FactoryBean<Codec> {
    val log by getLogger()

    @Autowired
    private lateinit var setting: SocketSetting

    override fun getObject(): Codec {
        val codec = Codec::class.java.subTypes().map { it.kotlin.objectInstance }.find { it?.name.equals(setting.codec, true) }
        codec?.let { log.info("Using codec: {}", codec.javaClass.simpleName) }
        return codec ?: throw NoSuchCodecException("Codec[${setting.codec}] not found in ${Codec::class.java.subTypes().map { it.kotlin.objectInstance?.name }.filterNotNull()}")
    }

    override fun isSingleton(): Boolean = true

    override fun getObjectType(): Class<*>? = Codec::class.java
}

class NoSuchCodecException(msg: String) : RuntimeException(msg)

interface Codec {
    val name: String
    fun encode(obj: Any?): ByteArray
    fun <T> decode(data: ByteArray, type: Class<T>): T
}

object ProtoBufCodec : Codec {
    override val name: String = "protobuf"

    private val defaultInstances = ConcurrentHashMap<Class<out MessageLite>, MessageLite>()

    private fun <T : MessageLite> defaultInstance(type: Class<out T>): MessageLite = defaultInstances.computeIfAbsent(type, { it.getMethod("getDefaultInstance").invoke(null) as T }) as T

    override fun encode(obj: Any?): ByteArray = (obj as? MessageLite )?.toByteArray() ?: emptyByteArray

    override fun <T> decode(data: ByteArray, type: Class<T>): T {
        return defaultInstance(type.asSubclass(MessageLite::class.java)).newBuilderForType().mergeFrom(data).build() as T
    }

    inline fun <reified T : MessageLite> decode(data: ByteArray): T = this.decode(data, T::class.java)
}

object StringCodec : Codec {
    override val name: String = "string"
    override fun encode(obj: Any?): ByteArray = obj?.toString()?.toByteArray() ?: emptyByteArray

    override fun <T> decode(data: ByteArray, type: Class<T>): T = String(data) as T

    fun decode(data: ByteArray) = String(data)
}

object ProtostuffCodec : Codec {
    override val name: String = "protostuff"
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
    override val name: String = "protostuffprotobuf"
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