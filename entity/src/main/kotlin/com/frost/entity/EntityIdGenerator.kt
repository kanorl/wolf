package com.frost.entity

interface EntityIdGenerator {
    fun <T : IEntity<Long>> next(entityClass: Class<T>, platform: Short, server: Short): Long
}