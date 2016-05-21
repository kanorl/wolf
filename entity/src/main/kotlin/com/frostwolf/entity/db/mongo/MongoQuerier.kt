package com.frostwolf.entity.db.mongo

import com.frostwolf.entity.IEntity
import com.frostwolf.entity.db.Querier
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class MongoQuerier : Querier {

    @Autowired
    private lateinit var template: MongoTemplate

    override fun <T : IEntity<*>> one(id: Any, clazz: Class<T>): T? = template.findById(id, clazz)

    override fun <T : IEntity<*>> all(clazz: Class<T>): List<T>? = template.findAll(clazz)

    override fun <T : IEntity<*>> query(clazz: Class<T>, where: String, fields: List<String>): List<T> {
        val query = Query.query(Criteria.where(where))
        fields.forEach { query.fields().include(it) }
        return template.find(query, clazz)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : IEntity<*>, R> query(clazz: Class<T>, projections: Any?, where: Any?, order: Any?, limit: Int?, wrapper: ((Any) -> R?)?): List<R> {
        val c = template.getCollection(template.getCollectionName(clazz))
        val query = where as? BasicDBObject ?: BasicDBObject()
        val find = c.find(query, projections as? BasicDBObject)
        (order as? BasicDBObject)?.let { find.sort(it) }
        limit?.let { find.limit(limit) }
        val converter: (Any) -> R? = wrapper ?: { any -> template.converter.read(clazz, any as DBObject) as R }
        return find.map { converter(it)!! }
    }
}