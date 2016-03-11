package com.frost.io.netty.codec

import com.frost.common.lang.emptyByteArray
import com.frost.common.reflect.subTypes
import com.frost.io.netty.config.SocketSetting
import com.google.protobuf.MessageLite
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

interface Codec<T> {
    fun encode(obj: T?): ByteArray
    fun decode(data: ByteArray, type: Class<T>): T
}

class ProtoBufCodec<T : MessageLite> : Codec<T> {

    companion object {
        private val defaultInstances = ConcurrentHashMap<Class<out MessageLite>, MessageLite>()
    }

    private fun defaultInstance(type: Class<T>): MessageLite = defaultInstances.computeIfAbsent(type, { type ->
        type.getMethod("getDefaultInstance").invoke(null) as MessageLite
    })

    override fun encode(obj: T?): ByteArray {
        return obj?.toByteArray() ?: emptyByteArray
    }

    @Suppress("UNCHECKED_CAST")
    override fun decode(data: ByteArray, type: Class<T>): T = defaultInstance(type).newBuilderForType().mergeFrom(data).build() as T
}

@Component
class CodecFactoryBean : FactoryBean<Codec<*>> {

    @Autowired
    private lateinit var setting: SocketSetting

    override fun getObjectType(): Class<*> = Codec::class.java

    override fun getObject(): Codec<*> = try {
        Codec::class.java.subTypes().first { it.simpleName.startsWith(setting.codec, true) }.newInstance()
    } catch(e: Exception) {
        throw IllegalStateException("Codec[${setting.codec}] not found in ${Codec::class.java.subTypes().map { it.simpleName }}")
    }

    override fun isSingleton(): Boolean = true
}