package com.frostwolf.entity.db

import com.frostwolf.entity.IEntity

interface Persistence {

    fun <T : IEntity<*>> save(entity: T)

    fun <T : IEntity<*>> remove(entity: T)

    fun <T : IEntity<*>> update(entity: T)
}