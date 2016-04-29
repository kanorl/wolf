package com.frostwolf.io.netty.handler

import com.frostwolf.common.concurrent.ExecutorContext
import com.frostwolf.common.logging.getLogger
import com.frostwolf.io.Request
import com.frostwolf.io.Response
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
        val command = request.command
        if (!manager.accessible(identity, command)) {
            logger.debug("Access denied: {} from {} access {}", identity, ctx.channel(), command)
            return;
        }
        val handler = manager.handler(command)
        if (handler == null) {
            logger.error("No handler for command[{}]", command)
            return
        }
        val action = {
            var result: Any? = null
            try {
                result = handler.invoke(request, ctx.channel())
            } catch(e: Exception) {
                logger.error("Request[cmd=$command, identity=${ctx.channel().identity()}] process failed", e)
            }
            if (!handler.responseOmit && result is Result<*>?) {
                ctx.writeAndFlush(Response(command, result?.value, result?.code ?: -1))
            }
        }

        val sequenceNo = manager.sequenceNo(command)
        if (sequenceNo != null) {
            ExecutorContext.submit(sequenceNo, action)
        } else if (identity != null) {
            ExecutorContext.submit(identity, action)
        } else {
            ExecutorContext.submit(action)
        }
    }
}