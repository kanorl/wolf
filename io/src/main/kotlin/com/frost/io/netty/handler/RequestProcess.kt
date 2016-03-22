package com.frost.io.netty.handler

import com.frost.common.function.invoke
import com.frost.common.logging.getLogger
import com.frost.common.reflect.genericTypes
import com.frost.common.reflect.safeGet
import com.frost.io.*
import com.frost.io.netty.ChannelIdParam
import com.frost.io.netty.ChannelParam
import com.frost.io.netty.IdentityParam
import com.frost.io.netty.RequestParam
import com.google.common.collect.Sets
import io.netty.channel.Channel
import io.netty.channel.ChannelId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import java.util.concurrent.ConcurrentHashMap

class FunctionInvoker(val func: Function<Result<*>?>, val params: Array<Param<out Any>>, val responseOmit: Boolean) {
    fun invoke(request: Request): Result<*>? {
        return func.invoke(params.map { it.getValue(request) }.toTypedArray())
    }
}

final class Result<T>(val code: Int, val value: T? = null) {
    val success = code == 0
    val failure = !success
    inline fun onSuccess(op: () -> Unit) = if (success) op() else Unit
    inline fun onFail(op: () -> Unit) = if (failure) op() else Unit
}

fun error(code: Int): Result<Unit> = Result(code)

@Suppress("UNCHECKED_CAST")
@Component
class InvokerManager : BeanPostProcessor {
    val logger by getLogger()

    @Autowired
    private lateinit var codec: Codec<Any>

    private val invokers = ConcurrentHashMap<Command, FunctionInvoker>()
    private val identityNotRequiredCommands = Sets.newConcurrentHashSet<Command>()

    override fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean;
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val type = bean.javaClass
        val annotation = type.getAnnotation(Module::class.java) ?: return bean
        val module = annotation.value;
        val identityRequired = type.getAnnotation(IdentityRequired::class.java)
        val identityNotRequired = identityRequired != null && !identityRequired.value
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
                    prev ?: throw IllegalStateException("Duplicate function $command")
                    if (it.isAnnotationPresent(IdentityRequired::class.java) && !it.getAnnotation(IdentityRequired::class.java).value) {
                        identityNotRequiredCommands.add(command)
                    } else if (!it.isAnnotationPresent(IdentityRequired::class.java) && identityNotRequired) {
                        identityNotRequiredCommands.add(command)
                    }
                },
                { it.isAnnotationPresent(Cmd::class.java) && Function::class.java.isAssignableFrom(it.type) }
        )
        return bean;
    }

    fun replace(command: Command, invoker: FunctionInvoker) {
        invokers.put(command, invoker)
        logger.error("Invoker[{}] replaced", command)
    }

    fun invoker(command: Command): FunctionInvoker? = invokers[command];

    fun identityRequired(command: Command): Boolean = !identityNotRequiredCommands.contains(command)
}