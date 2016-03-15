package com.frost

import com.frost.entity.EntityIdGenerator
import com.frost.entity.User
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * @author nevermore
 */
@Configuration
@ComponentScan("com.frost")
@EnableAutoConfiguration
open class Test {

}

fun main(args: Array<String>) {
    var context: ConfigurableApplicationContext? = null
    try {
        context = SpringApplication.run(Test::class.java)
    } catch(e: Exception) {
        System.exit(-1)
    }

    context!!.registerShutdownHook()
    context.start()

    val generator = context.getBean(EntityIdGenerator::class.java)
    println(generator.next(User::class.java, 1, 1))

    TimeUnit.SECONDS.sleep(60)
    context.close()
}
