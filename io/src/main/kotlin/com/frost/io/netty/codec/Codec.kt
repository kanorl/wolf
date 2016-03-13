package com.frost.io.netty.codec

import com.frost.common.lang.emptyByteArray
import com.frost.common.logging.getLogger
import com.frost.common.reflect.subTypes
import com.frost.io.netty.config.SocketSetting
import com.google.protobuf.MessageLite
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

interface Codec<in In> {
    val name: String
    fun encode(obj: In?): ByteArray
    fun <Out : In> decode(data: ByteArray, type: Class<Out>): Out
}

@Suppress("UNCHECKED_CAST")
object ProtoBufCodec : Codec<MessageLite> {
    override val name: String = "protobuf"

    private val defaultInstances = ConcurrentHashMap<Class<out MessageLite>, MessageLite>()

    private fun <T : MessageLite> defaultInstance(type: Class<T>): T = defaultInstances.computeIfAbsent(type, { it.getMethod("getDefaultInstance").invoke(null) as T }) as T

    override fun encode(obj: MessageLite?): ByteArray {
        return obj?.toByteArray() ?: emptyByteArray
    }

    override fun <T : MessageLite> decode(data: ByteArray, type: Class<T>): T = defaultInstance(type).newBuilderForType().mergeFrom(data).build() as T
}

@Component
class CodecFactoryBean : FactoryBean<Codec<*>> {
    val logger by getLogger()

    @Autowired
    private lateinit var setting: SocketSetting

    override fun getObjectType(): Class<*> = Codec::class.java

    override fun getObject(): Codec<*> {
        val codec = Codec::class.java.subTypes().map { it.kotlin.objectInstance }.first { it?.name == setting.codec }
        codec?: throw IllegalStateException("Codec[${setting.codec}] not found. Available Codecs are ${Codec::class.java.subTypes().map { it.kotlin.objectInstance?.name }}")
        logger.info("Codec is {}", codec.javaClass.simpleName)
        return codec
    }

    override fun isSingleton(): Boolean = true
}