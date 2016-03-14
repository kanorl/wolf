package com.frost.entity.db

import com.frost.entity.AbstractEntity

interface Querier {
    fun <T : AbstractEntity<*>> one(id: Any, clazz: Class<T>): T?
    fun <T : AbstractEntity<*>> all(clazz: Class<T>): List<T>?
    fun <T : AbstractEntity<*>> max(clazz: Class<T>): T?
    fun <T : AbstractEntity<*>> query(clazz: Class<T>, where: String, vararg fields: String = arrayOf()): List<T>
}