package com.frostwolf.sample.module.pong

import com.frostwolf.common.concurrent.task
import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.scheduling.Scheduler
import com.frostwolf.common.time.minutes
import com.frostwolf.entity.cache.EntityCache
import com.frostwolf.io.*
import com.frostwolf.io.netty.handler.Result
import com.frostwolf.io.netty.handler.success
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PostConstruct

@Module(1)
@Identities(arrayOf(Identity.Unknown::class))
class Pong {
    val logger by getLogger()

    @Autowired
    private lateinit var ctx: ConfigurableApplicationContext
    @Autowired
    private lateinit var scheduler: Scheduler

    @PostConstruct
    fun init() {
        scheduler.scheduleOnce(5.minutes, task(name = "close"){ ctx.close() })
    }

    private lateinit var users: EntityCache<Long, User>

    private val counter = AtomicInteger()
    private var n = 0

    @Cmd(1)
    @Sync
    val pong: (String) -> Result<String> = {
        n++
        val increment = counter.incrementAndGet()
        logger.info("$it-$n-$increment")

        val id = increment % 1000L
        val user = users.getOrCreate(id, { User(id, id.toString(), 10) })
        user.age = counter.get()
        user.save()
        success("pong")
    }
}