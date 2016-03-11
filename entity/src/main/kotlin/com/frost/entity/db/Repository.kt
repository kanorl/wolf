package com.frost.entity.db

import com.frost.entity.Entity
import java.io.Serializable

interface Repository {

    fun <T : Entity<*>> save(entity: T)

    fun <T : Entity<*>> remove(entity: T)

    fun <T : Entity<*>> update(entity: T)

    fun <PK : Serializable, T : Entity<PK>> get(id: PK, type: Class<T>): T?

    fun <T : Entity<*>> getAll(type: Class<T>, where: String = ""): List<T>

    fun <T : Entity<*>, R> query(type: Class<T>, where: String, converter: ((Any?) -> R)? = null): R
}