package com.frost.entity.db

import com.frost.entity.AbstractEntity

interface Persistence {

    fun <T : AbstractEntity<*>> save(entity: T)

    fun <T : AbstractEntity<*>> remove(entity: T)

    fun <T : AbstractEntity<*>> update(entity: T)
}