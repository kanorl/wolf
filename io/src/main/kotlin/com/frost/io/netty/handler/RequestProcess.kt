package com.frost.io.netty.handler

import com.frost.common.function.invoke
import com.frost.common.logging.getLogger
import com.frost.common.reflect.genericTypes
import com.frost.common.reflect.safeGet
import com.frost.io.*
import com.frost.io.netty.*
import io.netty.channel.Channel
import io.netty.channel.ChannelId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils

class RequestHandler(val func: Function<*>, val params: Array<Param<out Any>>, val responseOmit: Boolean) {
    fun invoke(request: Request<*>, channel: Channel): Any? {
        return func.invoke(params.map { it.getValue(request, channel) })
    }
}

data class Result<T>(val code: Int, val value: T? = null) {
    val success: Boolean
        get() = code == 0
    val failure: Boolean
        get() = !success

    inline fun onSuccess(op: () -> Unit) = if (success) op() else Unit
    inline fun onFailure(op: () -> Unit) = if (failure) op() else Unit
}

fun error(code: Int): Result<Unit> = Result(code)
inline fun <reified T> success(value: T? = null): Result<T> = Result(0, value)

@Suppress("UNCHECKED_CAST")
@Component
class RequestHandlerManager : BeanPostProcessor {
    val logger by getLogger()

    @Autowired
    private lateinit var codec: Codec

    private val handlers = hashMapOf<Command, RequestHandler>()
    private var commandPermission = mapOf<Command, List<Class<out Identity>>>()
    private var syncCommands = setOf<Command>()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean;
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val type = bean.javaClass
        val annotation = type.getAnnotation(Module::class.java) ?: return bean
        val module = annotation.value;
        val classIdentities = type.getAnnotation(Identities::class.java)?.value ?: emptyArray()
        ReflectionUtils.doWithFields(type,
                {
                    val cmdAnno = it.getAnnotation(Cmd::class.java)
                    val cmd = cmdAnno.value
                    val func = it.safeGet<Function<Result<*>>>(bean)!!
                    val typeArgs = genericTypes(it.type, func.javaClass)
                    val parameterTypes = typeArgs.sliceArray(0 until typeArgs.lastIndex)
                    val params = parameterTypes.map { pType ->
                        when (pType) {
                            Long::class.java -> IdentityParam()
                            Channel::class.java -> ChannelParam()
                            ChannelId::class.java -> ChannelIdParam()
                            else -> RequestParam(pType as Class<Any>, { data, type -> codec.decode(data, type) })
                        }
                    }.toTypedArray()
                    val command = Command(module, cmd)
                    val prev = handlers.put(command, RequestHandler(func, params, typeArgs.last() == javaClass))
                    prev?.let { throw IllegalStateException("Duplicate function $command") }
                    val fieldIdentities = it.getAnnotation(Identities::class.java)?.value ?: emptyArray()
                    if (fieldIdentities.isNotEmpty()) {
                        commandPermission += (command to fieldIdentities.map { it.java } )
                    } else if (classIdentities.isNotEmpty()) {
                        commandPermission += (command to classIdentities.map { it.java })
                    } else {
                        commandPermission += (command to listOf(Identity.Companion.Player::class.java))
                    }
                    if (cmdAnno.sync) {
                        syncCommands += command
                    }
                },
                { it.isAnnotationPresent(Cmd::class.java) && Function::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }

    @Synchronized
    fun replace(command: Command, handler: RequestHandler) {
        handlers.put(command, handler)
        logger.error("Invoker[{}] replaced", command)
    }

    fun isSync(command: Command) = syncCommands.contains(command)

    fun handler(command: Command): RequestHandler? = handlers[command];

    fun accessible(identity: Identity?, command: Command): Boolean {
        val i = identity ?: Identity.Companion.Unknown
        return commandPermission[command]?.any { it.isAssignableFrom(i.javaClass) } ?: false
    }
}