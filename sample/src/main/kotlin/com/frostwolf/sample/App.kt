package com.frostwolf.sample

import com.frostwolf.common.logging.defaultLogger
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.util.ResourceUtils
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * @author nevermore
 */
@SpringBootApplication(scanBasePackages = arrayOf("com.frostwolf"))
open class App {

}

fun main(args: Array<String>) {
    loadSystemProperties()
    val context: ConfigurableApplicationContext
    try {
        context = SpringApplication.run(App::class.java)
        context.registerShutdownHook()
        context.start()
    } catch(e: Exception) {
        defaultLogger.error(e.message, e)
        exitProcess(-1)
    }

    while (context.isActive) {
        TimeUnit.SECONDS.sleep(10)
    }
}

fun loadSystemProperties() {
    val p = Properties()
    FileInputStream(ResourceUtils.getFile("classpath:system.properties")).use {
        p.load(it)
    }
    p.forEach {
        System.setProperty(it.key.toString(), it.value.toString())
    }
}
