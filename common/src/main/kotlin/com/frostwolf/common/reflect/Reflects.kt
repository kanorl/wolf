@file:Suppress("UNCHECKED_CAST")

package com.frostwolf.common.reflect

import net.jodah.typetools.TypeResolver
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun <T : Type> T.args(clazz: Class<*>) = TypeResolver.resolveRawArguments(this, clazz) ?: emptyArray()

fun Field.typeArgs(): List<Class<*>> {
    val genericType = this.genericType
    if (genericType !is ParameterizedType) return listOf()
    return genericType.actualTypeArguments.map { type -> type as Class<*> }
}

val reflections by lazy { Reflections("") }

fun <T : Any> Class<T>.subTypes(includeSelf: Boolean = false, excludeAbstract: Boolean = true): List<Class<out T>> {
    val subTypes = reflections.getSubTypesOf(this)
    if (includeSelf) subTypes += this
    return if (excludeAbstract) subTypes.filter { !it.isAbstract() } else subTypes.toList()
}

fun <T : Any> Class<T>.allFields() = ReflectionUtils.getAllFields(this)

fun <T> Field.safeGet(target: Any): T? {
    org.springframework.util.ReflectionUtils.makeAccessible(this)
    return this.get(target) as? T
}

fun Field.safeSet(target: Any, value: Any) {
    org.springframework.util.ReflectionUtils.makeAccessible(this)
    this.set(target, value)
}

fun <T : Any> Class<T>.isAbstract(): Boolean = Modifier.isAbstract(this.modifiers)