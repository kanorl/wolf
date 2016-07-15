package com.frostwolf.entity

interface EntityIdGenerator {
    fun <T : Entity<Long>> next(entityClass: Class<T>, platform: Short, server: Short): Long
}