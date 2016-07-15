package com.frostwolf.entity.db.mongo

import com.frostwolf.entity.Entity
import com.frostwolf.entity.EntityIdGenerator
import com.frostwolf.entity.EntitySetting
import com.frostwolf.entity.db.Querier
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class MongoEntityIdGenerator : EntityIdGenerator {
    @Autowired
    private lateinit var setting: EntitySetting
    @Autowired
    private lateinit var querier: Querier

    private val cache = ConcurrentHashMap<Triple<Class<out Entity<*>>, Short, Short>, AtomicLong>()

    override fun <T : Entity<Long>> next(entityClass: Class<T>, platform: Short, server: Short): Long {
        val key = Triple(entityClass, platform, server)
        val next = cache.computeIfAbsent(key, {
            val range = range(platform, server)
            val where = QueryBuilder.start("_id").greaterThanEquals(range.first).and("_id").lessThanEquals(range.last).get()
            val result = querier.query(it.first, BasicDBObject("_id", 1), where, BasicDBObject("_id", -1), 1, { any -> (any as DBObject).get("_id") as Long })
            val current = if (result.isEmpty()) range.first - 1 else result[0]
            AtomicLong(current)
        }).incrementAndGet()
        val range = range(platform, server)
        check(range.contains(next), { "entity[${entityClass.simpleName}] id[$next] out of range[$range]" })
        return next
    }

    fun range(platform: Short, server: Short): LongRange {
        val bitsMax = 63
        val min = platform.toLong() shl (bitsMax - setting.platformBits) or (server.toLong() shl (bitsMax - setting.platformBits - setting.serverBits))
        val max = min or (1L shl ((bitsMax - setting.platformBits - setting.serverBits))) - 1
        return min..max
    }
}