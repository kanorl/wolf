package com.frost.entity.db.mongo

import com.frost.entity.IEntity
import com.frost.entity.db.Persistence
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(Persistence::class)
class MongoPersistence : Persistence {

    @Autowired
    lateinit var template: MongoTemplate

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