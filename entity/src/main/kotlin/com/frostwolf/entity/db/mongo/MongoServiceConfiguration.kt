package com.frostwolf.entity.db.mongo

import com.frostwolf.entity.EntityIdGenerator
import com.frostwolf.entity.db.Persistence
import com.frostwolf.entity.db.Querier
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