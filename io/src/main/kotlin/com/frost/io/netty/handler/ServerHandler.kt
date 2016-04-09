package com.frost.io.netty.handler

import com.frost.common.logging.getLogger
import com.frost.io.Request
import com.frost.io.Response
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ServerHandler : SimpleChannelInboundHandler<Request<ByteArray>>() {
    val logger by getLogger()

    @Autowired
    private lateinit var manager: RequestHandlerManager

    override fun channelRead0(ctx: ChannelHandlerContext, request: Request<ByteArray>) {
        val identity = ctx.identity()
        if (!manager.accessible(identity, request.command)) {
            logger.debug("Access denied: {} from {} access {}", identity, ctx.channel(), request.command)
            return;
        }
        val handler = manager.handler(request.command)
        if (handler == null) {
            logger.error("No handler for command[{}]", request.command)
            return
        }

        var result: Any? = null
        try {
            result = handler.invoke(request, ctx.channel())
        } catch(e: Exception) {
            logger.error("Request[cmd=${request.command}, identity=${ctx.channel().identity()}] process failed", e)
        }
        if (handler.responseOmit) {
            return
        }
        if (result !is Result<*>?) {
            return
        }

        ctx.writeAndFlush(Response(request.command, result?.value, result?.code ?: -1))
    }
}