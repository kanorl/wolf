package com.frost.entity.db.mongo

import com.frost.entity.AbstractEntity
import com.frost.entity.db.Persist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class MongoPersistence : Persist {

    @Autowired
    lateinit var template: MongoTemplate

    override fun <T : AbstractEntity<*>> save(entity: T) {
        template.save(entity)
    }

    override fun <T : AbstractEntity<*>> remove(entity: T) {
        template.remove(entity)
    }

    override fun <T : AbstractEntity<*>> update(entity: T) {
        template.save(entity)
    }
}