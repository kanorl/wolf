package com.frostwolf.entity.db

import com.frostwolf.entity.Entity

interface Querier {
    fun <T : Entity<*>> one(id: Any, clazz: Class<T>): T?
    fun <T : Entity<*>> all(clazz: Class<T>): List<T>?
    fun <T : Entity<*>> query(clazz: Class<T>, where: String, fields: List<String> = emptyList()): List<T>
    fun <T : Entity<*>, R> query(clazz: Class<T>, projections: Any? = null, where: Any? = null, order: Any? = null, limit: Int? = null, wrapper: ((Any) -> R?)? = null): List<R>
}