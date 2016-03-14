package com.frost.entity.db.mongo

import com.frost.entity.AbstractEntity
import com.frost.entity.db.Querier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class MongoQuerier : Querier {

    @Autowired
    lateinit var template: MongoTemplate

    override fun <T : AbstractEntity<*>> one(id: Any, clazz: Class<T>): T? = template.findById(id, clazz)

    override fun <T : AbstractEntity<*>> all(clazz: Class<T>): List<T>? = template.findAll(clazz)

    override fun <T : AbstractEntity<*>> max(clazz: Class<T>): T? = null

    override fun <T : AbstractEntity<*>> query(clazz: Class<T>, where: String, vararg fields: String): List<T> {
        return emptyList()
    }
}