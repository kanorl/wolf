package com.frost.entity.db.mongo

import com.frost.entity.EntityIdGenerator
import com.frost.entity.EntitySetting
import com.frost.entity.IEntity
import com.frost.entity.db.Querier
import com.google.common.base.Function
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.QueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.atomic.AtomicLong

class MongoEntityIdGenerator : EntityIdGenerator {
    @Autowired
    private lateinit var setting: EntitySetting
    @Autowired
    private lateinit var querier: Querier

    private val cache: LoadingCache<Class<out IEntity<Long>>, LoadingCache<Short, LoadingCache<Short, AtomicLong>>> = CacheBuilder.newBuilder().build(
            CacheLoader.from(Function { clazz ->
                CacheBuilder.newBuilder().build(
                        CacheLoader.from (Function { platform ->
                            CacheBuilder.newBuilder().build(
                                    CacheLoader.from (Function { server ->
                                        val range = range(platform, server)
                                        val where = QueryBuilder.start("_id").greaterThanEquals(range.first).and("_id").lessThanEquals(range.last).get()
                                        val result = querier.query(clazz, BasicDBObject("_id", 1), where, BasicDBObject("_id", -1), 1, { any -> (any as DBObject).get("_id") as Long })
                                        val next = if (result.isEmpty()) range.first else result[0] + 1
                                        AtomicLong(next)
                                    })
                            )
                        })
                )
            })
    )

    override fun <T : IEntity<Long>> next(entityClass: Class<T>, platform: Short, server: Short): Long {
        val current = cache.get(entityClass).get(platform).get(server)
        val next = current.andIncrement
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