package com.frost

import com.frost.common.event.Event
import com.frost.common.event.EventBus
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
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

    context.getBean(EventBus::class.java).post(MyEvent())

    TimeUnit.SECONDS.sleep(10)
    context.close()
}

open class MyEvent : Event {

}

@Component
@Primary
class A : MyEvent() {

}

@Component
class B : MyEvent() {

}