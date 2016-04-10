package com.frost.io

import com.frost.common.logging.getLogger
import com.frost.io.netty.handler.Result
import com.frost.io.netty.handler.success
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ConfigurableApplicationContext

@Module(1)
@Identities(arrayOf(Identity.Companion.Unknown::class))
class Pong {
    val logger by getLogger()

    @Autowired
    private lateinit var ctx: ConfigurableApplicationContext

    @Cmd(1)
    val pong: (String) -> Result<String> = {
        logger.info(it)
        success("pong")
    }
}