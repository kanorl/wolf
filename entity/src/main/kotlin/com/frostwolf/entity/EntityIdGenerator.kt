package com.frostwolf.entity

interface EntityIdGenerator {
    fun <T : Entity<Long>> next(entityClass: Class<T>, platform: Short, server: Short): Long

    fun range(platform: Short, server: Short, platformBits: Int, serverBits: Int): LongRange {
        val bitsMax = 63
        val min = platform.toLong() shl (bitsMax - platformBits) or (server.toLong() shl (bitsMax - platformBits - serverBits))
        val max = min or (1L shl ((bitsMax - platformBits - serverBits))) - 1
        return min..max
    }
}