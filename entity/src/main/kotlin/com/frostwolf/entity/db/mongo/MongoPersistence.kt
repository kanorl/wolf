package com.frostwolf.entity.db.mongo

import com.frostwolf.entity.Entity
import com.frostwolf.entity.db.Persistence
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate

class MongoPersistence : Persistence {

    @Autowired
    private lateinit var template: MongoTemplate

    override fun <T : Entity<*>> save(entity: T) {
        template.save(entity)
    }

    override fun <T : Entity<*>> remove(entity: T) {
        template.remove(entity)
    }

    override fun <T : Entity<*>> update(entity: T) {
        template.save(entity)
    }
}