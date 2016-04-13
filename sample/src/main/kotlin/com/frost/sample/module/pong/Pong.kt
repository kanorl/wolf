package com.frost.sample.module.pong

import com.frost.common.logging.getLogger
import com.frost.common.scheduling.Scheduler
import com.frost.common.time.seconds
import com.frost.entity.cache.EntityCache
import com.frost.io.Cmd
import com.frost.io.Identities
import com.frost.io.Identity
import com.frost.io.Module
import com.frost.io.netty.handler.Result
import com.frost.io.netty.handler.success
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct

@Module(1)
@Identities(arrayOf(Identity.Companion.Unknown::class))
class Pong {
    val logger by getLogger()

    @Autowired
    private lateinit var ctx: ConfigurableApplicationContext
    @Autowired
    private lateinit var scheduler: Scheduler

    @PostConstruct
    fun init() {
        scheduler.scheduleOnce(60.seconds(), "close") { ctx.close() }
    }

    private lateinit var users: EntityCache<Long, User>

    private val counter = AtomicInteger()

    @Cmd(1)
    val pong: (String) -> Result<String> = {
        logger.info(it)

        val id = counter.andIncrement % 1000L
        val user = users.getOrCreate(id, { User(id, id.toString(), 10) })
        user.age = counter.get()
        user.save()
        success("pong")
    }
}