package com.frost.common.reflect

import net.jodah.typetools.TypeResolver
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

// TODO rename
fun genericTypes(type: Type, clazz: Class<*>): Array<Class<*>> = TypeResolver.resolveRawArguments(type, clazz) ?: emptyArray()

@Suppress("UNCHECKED_CAST")
fun Field.genericTypes(): List<Class<*>> {
    val genericType = this.genericType
    if (genericType !is ParameterizedType)
        throw IllegalStateException("Field's generic type is not ParameterizedType")
    return genericType.actualTypeArguments.map { type -> type as Class<*> }
}

fun Field.genericType(): Class<*> = genericTypes()[0]

fun <T : Any> T.genericType(clazz: Class<*>): Class<*> = genericTypes(clazz)[0]

fun <T : Any> T.genericTypes(clazz: Class<*>): List<Class<*>> {
    assert(clazz.isInstance(this))
    val pType = (if (clazz.isInterface) this.javaClass.genericInterfaces.find { t -> t is ParameterizedType && t.rawType == clazz } else this.javaClass.genericSuperclass) as? ParameterizedType
    return pType?.actualTypeArguments?.map { type -> type as Class<*> } ?: emptyList()
}

val reflections by lazy { Reflections("") }

fun <T : Any> Class<T>.subTypes(includeSelf: Boolean = false, excludeAbstract: Boolean = true): List<Class<out T>> {
    val subTypes = reflections.getSubTypesOf(this)
    if (includeSelf) subTypes += this
    return if (excludeAbstract) subTypes.filter { !it.isAbstract() } else subTypes.toList()
}

fun <T : Any> Class<T>.allFields() = ReflectionUtils.getAllFields(this)

@Suppress("UNCHECKED_CAST")
fun <T> Field.safeGet(target: Any): T? {
    org.springframework.util.ReflectionUtils.makeAccessible(this)
    return this.get(target) as? T
}

fun Field.safeSet(target: Any, value: Any) {
    org.springframework.util.ReflectionUtils.makeAccessible(this)
    this.set(target, value)
}

fun <T : Any> Class<T>.isAbstract(): Boolean = Modifier.isAbstract(this.modifiers)