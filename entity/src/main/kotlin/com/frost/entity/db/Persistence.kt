package com.frost.entity.db

import com.frost.entity.IEntity

interface Persistence {

    fun <T : IEntity<*>> save(entity: T)

    fun <T : IEntity<*>> remove(entity: T)

    fun <T : IEntity<*>> update(entity: T)
}