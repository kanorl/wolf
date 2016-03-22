package com.frost.entity.db.mongo

import com.frost.entity.EntityIdGenerator
import com.frost.entity.db.Persistence
import com.frost.entity.db.Querier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open internal class MongoServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean(EntityIdGenerator::class)
    open fun entityIdGenerator(): EntityIdGenerator = MongoEntityIdGenerator()

    @Bean
    @ConditionalOnMissingBean(Persistence::class)
    open fun persistence(): Persistence = MongoPersistence()

    @Bean
    @ConditionalOnMissingBean(Querier::class)
    open fun querier(): Querier = MongoQuerier()
}