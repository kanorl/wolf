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

class FunctionInvoker(val func: Function<*>, val params: Array<Param<out Any>>, val responseOmit: Boolean) {
    fun invoke(request: Request<*>, channel: Channel): Any? {
        return func.invoke(params.map { it.getValue(request, channel) }.toTypedArray())
    }
}

final class Result<T>(val code: Int, val value: T? = null) {
    val success = code == 0
    val failure = !success
    inline fun onSuccess(op: () -> Unit) = if (success) op() else Unit
    inline fun onFail(op: () -> Unit) = if (failure) op() else Unit
}

fun error(code: Int): Result<Unit> = Result(code)
inline fun <reified T> success(value: T? = null): Result<T> = Result(0, value)

@Suppress("UNCHECKED_CAST")
@Component
class InvokerManager : BeanPostProcessor {
    val logger by getLogger()

    @Autowired
    private lateinit var codec: Codec

    private val invokers = hashMapOf<Command, FunctionInvoker>()
    private val commandGroup = mul<Class<out Identity>, Command>()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean;
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val type = bean.javaClass
        val annotation = type.getAnnotation(Module::class.java) ?: return bean
        val module = annotation.value;
        val classAnnotations = type.getDeclaredAnnotationsByType(IdentityRequired::class.java).map { it.value }.let { if (it.isEmpty()) listOf(Identity.Companion.Player::class) else it }
        ReflectionUtils.doWithFields(type,
                {
                    val cmd = it.getAnnotation(Cmd::class.java)!!.value
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
                    val prev = invokers.put(command, FunctionInvoker(func, params, typeArgs.last() == javaClass))
                    prev?.let { throw IllegalStateException("Duplicate function $command") }
                    val fieldAnnotatins = it.getAnnotationsByType(IdentityRequired::class.java)

                    if (it.isAnnotationPresent(IdentityRequired::class.java)) {
                        commandGroup + (command to it.getAnnotation(IdentityRequired::class.java).value)
                    } else {
                        commandGroup + (command to identityType)
                    }
                },
                { it.isAnnotationPresent(Cmd::class.java) && Function::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }

    @Synchronized
    fun replace(command: Command, invoker: FunctionInvoker) {
        invokers.put(command, invoker)
        logger.error("Invoker[{}] replaced", command)
    }

    fun invoker(command: Command): FunctionInvoker? = invokers[command];

    fun checkAuth(command: Command, identity: Identity?): Boolean = identity != null && commandGroup[command]?.javaClass === identity.javaClass
}