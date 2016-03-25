package com.frost.io

import com.frost.common.logging.getLogger
import com.frost.io.netty.handler.Result
import com.frost.io.netty.handler.success

@Module(1)
@Identities(arrayOf(Identity::class))
class Pong {
    val logger by getLogger()

    @Cmd(1)
    val pong: (String) -> Result<String> = {
        logger.info(it)
        success("pong")
    }
}