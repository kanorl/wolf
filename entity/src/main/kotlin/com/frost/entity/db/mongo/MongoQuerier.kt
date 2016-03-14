package com.frost.entity.db.mongo

import com.frost.entity.AbstractEntity
import com.frost.entity.db.Querier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class MongoQuerier : Querier {

    @Autowired
    private lateinit var template: MongoTemplate

    override fun <T : AbstractEntity<*>> one(id: Any, clazz: Class<T>): T? = template.findById(id, clazz)

    override fun <T : AbstractEntity<*>> all(clazz: Class<T>): List<T>? = template.findAll(clazz)

    override fun <T : AbstractEntity<*>> max(clazz: Class<T>): T? {
        val query = BasicQuery("{}").with(Sort(Sort.Order(Sort.Direction.DESC, "_id"))).limit(1)
        return template.findOne(query, clazz)
    }

    override fun <T : AbstractEntity<*>> query(clazz: Class<T>, where: String, vararg fields: String): List<T> {
        val query = Query.query(Criteria.where(where))
        fields.forEach { query.fields().include(it) }
        return template.find(query, clazz)
    }
}