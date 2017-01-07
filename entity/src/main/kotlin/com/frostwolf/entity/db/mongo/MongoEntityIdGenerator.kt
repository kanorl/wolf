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
        val range = range(platform, server, setting.platformBits, setting.serverBits)
        val next = cache.computeIfAbsent(key, {
            val idField = "_id"
            val where = QueryBuilder.start(idField).greaterThanEquals(range.first).and(idField).lessThanEquals(range.last).get()
            val result = querier.query(it.first, BasicDBObject(idField, 1), where, BasicDBObject(idField, -1), 1, { any -> (any as DBObject).get(idField) as Long })
            val current = if (result.isEmpty()) range.first - 1 else result[0]
            AtomicLong(current)
        }).incrementAndGet()
        check(range.contains(next), { "entity[${entityClass.simpleName}] id[$next] out of range[$range]" })
        return next
    }
}