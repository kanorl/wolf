package com.frost.resource

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration
open class ResourceConfiguration {

    @Bean
    @ConditionalOnMissingBean(Validator::class)
    open fun validator(): Validator = LocalValidatorFactoryBean()

    @Bean
    @ConditionalOnMissingBean(Reader::class)
    open fun reader(): Reader = JsonReader
}