package com.frost.io.netty.codec

import com.frost.common.lang.emptyByteArray
import com.google.protobuf.MessageLite
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

interface Codec<in In> {
    fun encode(obj: In?): ByteArray
    fun <Out : In> decode(data: ByteArray, type: Class<Out>): Out
}

@Component
@ConditionalOnMissingBean(Codec::class)
@Suppress("UNCHECKED_CAST")
class ProtoBufCodec : Codec<MessageLite> {
    private val defaultInstances = ConcurrentHashMap<Class<out MessageLite>, MessageLite>()

    private fun <T : MessageLite> defaultInstance(type: Class<T>): T = defaultInstances.computeIfAbsent(type, { it.getMethod("getDefaultInstance").invoke(null) as T }) as T

    override fun encode(obj: MessageLite?): ByteArray {
        return obj?.toByteArray() ?: emptyByteArray
    }

    override fun <T : MessageLite> decode(data: ByteArray, type: Class<T>): T = defaultInstance(type).newBuilderForType().mergeFrom(data).build() as T
}