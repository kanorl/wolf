package com.frostwolf.sample.module.simpleconfig

import com.fasterxml.jackson.annotation.JsonCreator
import com.frostwolf.common.toJson
import com.frostwolf.common.toObj
import com.frostwolf.resource.Resource


class SimpleConfig() : Resource() {

    @JsonCreator
    constructor ( s: String) : this() {
        println(111111)
    }

    val value: String = ""
    @Transient
    var v: Any? = null
        private set

    inline fun <reified T : Any> get(): T {
        if (v == null) {
            v = value.toObj<T>()
        }
        return v as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(converter: (String) -> T): T {
        if (v == null) {
            v = converter(value)
        }
        return v as T
    }
}

fun main(args: Array<String>) {
    val json = SimpleConfig().toJson()
    json.toObj<SimpleConfig>()
}