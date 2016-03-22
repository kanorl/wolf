package com.frost.entity.db.mongo

import com.frost.entity.IEntity
import com.frost.entity.db.Persistence
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate

class MongoPersistence : Persistence {

    @Autowired
    private lateinit var template: MongoTemplate

    override fun <T : IEntity<*>> save(entity: T) {
        template.save(entity)
    }

    override fun <T : IEntity<*>> remove(entity: T) {
        template.remove(entity)
    }

    override fun <T : IEntity<*>> update(entity: T) {
        template.save(entity)
    }
}