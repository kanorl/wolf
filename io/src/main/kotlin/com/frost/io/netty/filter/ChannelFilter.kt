package com.frost.io.netty.filter

import com.frost.common.Ordered
import com.frost.io.netty.config.SocketSetting
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener.CLOSE
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.ipfilter.IpFilterRule
import io.netty.handler.ipfilter.IpFilterRuleType
import io.netty.handler.ipfilter.IpSubnetFilterRule
import org.springframework.beans.factory.annotation.Autowired
import java.net.InetSocketAddress
import java.net.SocketAddress
import javax.annotation.PostConstruct

@ChannelHandler.Sharable
abstract class ChannelFilter : ChannelInboundHandlerAdapter(), Ordered {

    @Autowired
    private lateinit var socketSetting: SocketSetting
    private lateinit var whiteListIpRules: List<IpFilterRule>;

    @PostConstruct
    private fun init() {
        whiteListIpRules = socketSetting.whiteList.map {
            IpSubnetFilterRule(it, 32 - it.toCharArray().filter { it -> it == '*' }.count() * 8, IpFilterRuleType.ACCEPT)
        }
    }

    override final fun channelRegistered(ctx: ChannelHandlerContext) {
        handleNewChannel(ctx)
        ctx.fireChannelRegistered()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        handleNewChannel(ctx)
        ctx.fireChannelActive()
    }

    private fun handleNewChannel(ctx: ChannelHandlerContext): Boolean {
        ctx.pipeline().remove(this)

        val remoteAddress: SocketAddress? = ctx.channel().remoteAddress()
        if (remoteAddress is InetSocketAddress && whiteListIpRules.any { rule -> rule.matches(remoteAddress) }) {
            return true
        }

        if (!accept(ctx)) {
            channelRejected(ctx)?.let { it.addListener(CLOSE) } ?: ctx.close()
        }

        return true
    }

    protected abstract fun accept(ctx: ChannelHandlerContext): Boolean

    protected open fun channelRejected(ctx: ChannelHandlerContext): ChannelFuture? = null
}