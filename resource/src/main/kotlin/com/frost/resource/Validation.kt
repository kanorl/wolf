package com.frost.resource

import cz.jirutka.validator.collection.CommonEachValidator
import cz.jirutka.validator.collection.constraints.EachConstraint
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Component
open class ResourceFactoryBean : FactoryBean<Validator> {
    override fun getObjectType(): Class<*>? = Validator::class.java

    override fun isSingleton(): Boolean = true

    override fun getObject(): Validator? = LocalValidatorFactoryBean()
}

class ResourceInvalidException : RuntimeException {
    companion object {
        private fun errorsToString(errors: List<ObjectError>): String {
            val b = StringBuilder()
            errors.forEach {
                b.append("\n")
                b.append(it.defaultMessage).append(": in object '").append(it.objectName).append("'")
                if (it is FieldError) b.append(" on field '").append(it.field).append("' which value is '").append(it.rejectedValue).append("'")
            }
            return b.toString()
        }
    }

    constructor(errors: List<ObjectError>) : super(errorsToString(errors))
}

class DuplicateResourceException(private val msg: String) : RuntimeException(msg) {
    constructor(clazz: Class<*>, duplicate: Collection<Int>) : this("Duplicate id$duplicate in ${clazz.simpleName}")
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = arrayOf(ReferToValidator::class))
annotation class ReferTo(val value: KClass<out Resource>, val message: String = "Referred resource not found", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = ReferTo::class)
@Constraint(validatedBy = arrayOf(CommonEachValidator::class))
annotation class EachReferTo(val value: KClass<out Resource>, val message: String = "Referred resource not found", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

class ReferToValidator : ConstraintValidator<ReferTo, Int> {

    @Autowired
    private lateinit var manager: ResourceManager

    private lateinit var refType: Class<*>

    override fun isValid(value: Int?, context: ConstraintValidatorContext?): Boolean {
        value ?: return true
        return manager.container(refType)?.contains(value) ?: false
    }

    override fun initialize(p: ReferTo) {
        refType = (p.value as Any).javaClass
    }
}