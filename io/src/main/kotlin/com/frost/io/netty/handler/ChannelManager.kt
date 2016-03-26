package com.frost.io.netty.handler

import com.frost.common.event.Event
import com.frost.common.event.EventBus
import com.frost.common.logging.getLogger
import com.frost.io.Identity
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

    val keepAliveMillis = 30000L

    @Autowired
    private lateinit var eventBus: EventBus

    private val channelGroup = ConcurrentHashMap<ChannelId, Channel>()
    private val identifiedChannelGroups = ConcurrentHashMap<Class<out Identity>, ConcurrentHashMap<Identity, Channel>>()
    private val remover = ChannelFutureListener { channelClosed(it.channel()) }
    private val createTimeKey = AttributeKey.valueOf<Long>("createTime")

    val cleanUpTask = GlobalEventExecutor.INSTANCE.scheduleWithFixedDelay({
        val now = System.currentTimeMillis()
        channelGroup.values.filter { !it.identified() && now - (it.attr(createTimeKey).get() ?: 0) > keepAliveMillis }.forEach {
            it.close()
            logger.info("Close channel due to identify timeout: {}", it)
        }
    }, keepAliveMillis, keepAliveMillis, TimeUnit.MILLISECONDS)

    @PreDestroy
    private fun onDestroy() {
        cleanUpTask.cancel(true)
    }

    private fun channelClosed(channel: Channel) {
        channelGroup.remove(channel.id(), channel)
        val identity = channel.attr(identityKey).get()
        identity?.let {
            identifiedChannelGroups.remove(it, channel)
            eventBus.post(ChannelClosedEvent(it))
        }
        channel.closeFuture().removeListener(remover)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        channel.attr(createTimeKey).setIfAbsent(System.currentTimeMillis())
        if (channelGroup.putIfAbsent(channel.id(), channel) != null) {
            logger.error("Duplicate Channel Id: {}", ctx.channel().id())
            ctx.close()
            return
        }
        channel.closeFuture().addListener(remover)
        super.channelActive(ctx)
    }

    fun bind(channelId: ChannelId, identity: Identity) {
        val channel = channelGroup[channelId] ?: return
        channel.identity(identity)

        val group = identifiedChannelGroups.computeIfAbsent(identity.javaClass, { ConcurrentHashMap<Identity, Channel>() })
        val prev = group.putIfAbsent(identity, channel)
        prev?.close()
        eventBus.post(ChannelBindEvent(identity, prev != null))
    }

    fun channel(channelId: ChannelId): Channel? = channelGroup[channelId]
    fun channels(): Collection<Channel> = Collections.unmodifiableCollection(channelGroup.values)
    fun channel(identity: Identity): Channel? = identifiedChannelGroups[identity.javaClass]?.get(identity)
    fun channels(type: Class<out Identity>): Collection<Channel> = Collections.unmodifiableCollection(identifiedChannelGroups[type]?.values ?: emptyList())
}

val identityKey = AttributeKey.valueOf<Identity>("identity")
fun Channel.identity(): Identity? {
    return this.attr(identityKey).get()
}

private fun Channel.removeIdentity() {
    return this.attr(identityKey).remove()
}

private fun Channel.identity(identity: Identity) {
    this.attr(identityKey).setIfAbsent(identity)?.let { throw IllegalStateException("identity[$identity] is already set.") }
}

fun Channel.identified(): Boolean = this.identity() != null

data class ChannelClosedEvent(val identity: Identity) : Event
data class ChannelBindEvent(val identity: Identity, val rebind: Boolean) : Event

