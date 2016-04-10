
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.TimeUnit

/**
 * @author nevermore
 */
@SpringBootApplication(scanBasePackages = arrayOf("com.frost"))
open class Test {

}

fun main(args: Array<String>) {
    // -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
    System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
    var context: ConfigurableApplicationContext? = null
    try {
        context = SpringApplication.run(Test::class.java)
    } catch(e: Exception) {
        System.exit(-1)
    }

    context!!.registerShutdownHook()
    context.start()

    while (context.isActive) {
        TimeUnit.SECONDS.sleep(10)
    }
    context.close()
}
