package com.frostwolf.entity.db

import com.frostwolf.entity.Entity

interface Persistence {

    fun <T : Entity<*>> save(entity: T)

    fun <T : Entity<*>> remove(entity: T)

    fun <T : Entity<*>> update(entity: T)
}