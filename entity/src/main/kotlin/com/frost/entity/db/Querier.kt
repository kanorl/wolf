package com.frost.entity.db

import com.frost.entity.IEntity

interface Querier {
    fun <T : IEntity<*>> one(id: Any, clazz: Class<T>): T?
    fun <T : IEntity<*>> all(clazz: Class<T>): List<T>?
    fun <T : IEntity<*>> query(clazz: Class<T>, where: String, fields: List<String> = emptyList()): List<T>
    fun <T : IEntity<*>, R> query(clazz: Class<T>, projections: Any? = null, where: Any? = null, order: Any? = null, limit: Int? = null, wrapper: ((Any) -> R?)? = null): List<R>
}