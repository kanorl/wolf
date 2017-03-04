package com.frostwolf.sample.module.simpleconfig

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.frostwolf.common.jacksonObjectMapper
import com.frostwolf.common.reflect.`as`
import com.frostwolf.common.toJson
import com.frostwolf.common.toObj
import com.frostwolf.resource.Resource
import java.text.NumberFormat
import java.text.ParseException

@Suppress("UNCHECKED_CAST")
class SimpleConfig : Resource() {

    @JsonProperty("value")
    val stringValue: String = ""
    @Transient
    private var value: Any? = null

    val numberValue: Number
        get() = value!!.`as`<Number>()
    val booleanValue
        get() = numberValue != 0

    fun <T> get(typeRef: () -> TypeReference<T>): T {
        if (value == null) {
            value = jacksonObjectMapper.readValue(stringValue, typeRef())
        }
        return value as T
    }

    inline fun <reified T> get(): T {
        return get({ object : TypeReference<T>() {} })
    }

    override fun afterPropertiesSet() {
        if (value != null) return
        if (stringValue.isEmpty()) {
            value = 0 as Number
        } else {
            try {
                value = NumberFormat.getNumberInstance().parse(stringValue)
            } catch (e: ParseException) {
                try {
                    jacksonObjectMapper.readTree(stringValue)
                } catch (e: Exception) {
                    value = stringValue
                }
            }
        }
    }

    override fun toString(): String {
        return "${this::class.java.simpleName}($stringValue)"
    }
}

fun main(args: Array<String>) {
    val json = """{"value": "[{\"value\": \"[1,2,3]\"},{\"value\": 1}]" }"""
    val cfg = json.toObj<SimpleConfig>()
    val value = cfg.get<List<SimpleConfig>>()
    println(value)
    println(cfg.toJson())
}