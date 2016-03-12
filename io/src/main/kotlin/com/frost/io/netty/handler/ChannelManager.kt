package com.frost.io.netty.handler

import com.frost.common.event.Event
import com.frost.common.event.EventBus
import com.frost.common.logging.getLogger
import io.netty.channel.*
import io.netty.util.AttributeKey
import io.netty.util.concurrent.GlobalEventExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy

@Component
@ChannelHandler.Sharable
class ChannelManager : ChannelDuplexHandler() {
    val logger by getLogger() // TODO session log

    val keepAlive = 30L

    @Autowired
    private lateinit var eventBus: EventBus

    private val anonymousChannels = ConcurrentHashMap<ChannelId, Channel>()
    private val identifiedChannels = ConcurrentHashMap<Long, Channel>()

    val cleanUpTask = GlobalEventExecutor.INSTANCE.scheduleWithFixedDelay({ }, keepAlive, keepAlive, TimeUnit.SECONDS)

    @PreDestroy
    private fun onDestroy() {
        cleanUpTask.cancel(true)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        if (anonymousChannels.putIfAbsent(channel.id(), channel) != null) {
            logger.error("Duplicate Channel Id: {}", ctx.channel().id())
            ctx.close()
        } else {
            super.channelActive(ctx)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        anonymousChannels.remove(channel.id())
        val identity = channel.identity()
        identity?.let { identifiedChannels.remove(it)?.let { eventBus.post(ChannelClosedEvent(identity)) } }
        super.channelInactive(ctx)
    }

    fun identify(channelId: ChannelId, identity: Long) {
        val channel = anonymousChannels[channelId] ?: return
        val prev = identifiedChannels.replace(identity, channel)
        prev?.removeIdentity()
        prev?.close()
        // check after add
        if (!channel.isOpen || anonymousChannels[channelId] != channel) {
            identifiedChannels.remove(identity, channel)
            return
        }
        eventBus.post(ChannelBindEvent(identity, prev != null))
    }

    fun channel(channelId: ChannelId): Channel? = anonymousChannels[channelId]

    fun channel(id: Long): Channel? = identifiedChannels[id]

    fun onlinePlayerIds() = Collections.unmodifiableSet(identifiedChannels.keys)
}

val identityKey = AttributeKey.valueOf<Long>("identity")
fun Channel.identity(): Long? {
    return this.attr(identityKey).get()
}

fun Channel.removeIdentity() {
    return this.attr(identityKey).remove()
}

fun Channel.identity(identity: Long) {
    this.attr(identityKey).setIfAbsent(identity) ?: throw IllegalStateException("identity[$identity] is already set.")
}

fun Channel.identified(): Boolean {
    return this.identity() != null
}

data class ChannelClosedEvent(val identity: Long) : Event
data class ChannelBindEvent(val identity: Long, val rebind: Boolean) : Event