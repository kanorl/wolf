package com.frost.sample

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ResourceUtils
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author nevermore
 */
@SpringBootApplication(scanBasePackages = arrayOf("com.frost"))
open class App {

}

fun main(args: Array<String>) {
    loadSystemProperties()
    val context: ConfigurableApplicationContext
    try {
        context = SpringApplication.run(App::class.java)
    } catch(e: Exception) {
        throw e
    }

    context.registerShutdownHook()
    context.start()

    while (context.isActive) {
        TimeUnit.SECONDS.sleep(10)
    }
}

fun loadSystemProperties() {
    val p = Properties()
    FileInputStream(ResourceUtils.getFile("classpath:system.properties")).use { p.load(it) }
    for ((k, v) in p) {
        System.setProperty(k.toString(), v.toString())
    }
}
