package com.frost.resource

import cz.jirutka.validator.collection.CommonEachValidator
import cz.jirutka.validator.collection.constraints.EachConstraint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import javax.inject.Inject
import javax.validation.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.reflect.KClass

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
annotation class ReferTo(val value: KClass<out Resource>, val message: String = "{javax.validation.constraints.ReferTo.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = ReferTo::class)
@Constraint(validatedBy = arrayOf(CommonEachValidator::class))
annotation class EachReferTo(val value: KClass<out Resource>, val message: String = "", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class MapValueConstraint

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = Min::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
annotation class KeysMin(val value: Long, val message: String = "{javax.validation.constraints.MapKey.message}{javax.validation.constraints.Min.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = Max::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
annotation class KeysMax(val value: Long, val message: String = "{javax.validation.constraints.MapKey.message}{javax.validation.constraints.Max.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = ReferTo::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
annotation class KeysReferTo(val value: KClass<out Resource>, val message: String = "{javax.validation.constraints.MapKey.message}{javax.validation.constraints.ReferTo.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = Min::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
annotation class ValuesMin(val value: Long, val message: String = "{javax.validation.constraints.MapValue.message}{javax.validation.constraints.Min.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = Max::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
annotation class ValuesMax(val value: Long, val message: String = "{javax.validation.constraints.MapValue.message}{javax.validation.constraints.Max.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = ReferTo::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
annotation class ValuesReferTo(val value: KClass<out Resource>, val message: String = "{javax.validation.constraints.MapValue.message}{javax.validation.constraints.ReferTo.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

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

class MapKeyValueValidator : ConstraintValidator<Annotation, Map<*, *>> {
    @Inject
    private var factory: ValidatorFactory? = null
    private var commonEachValidator: CommonEachValidator? = null
    private var validValue = false

    override fun initialize(a: Annotation) {
        validValue = a.annotationClass.java.isAnnotationPresent(MapValueConstraint::class.java)
        if (factory == null) {
            factory = Validation.buildDefaultValidatorFactory()
        }
        if (commonEachValidator == null) {
            commonEachValidator = factory!!.constraintValidatorFactory.getInstance(CommonEachValidator::class.java)
        }
        commonEachValidator!!.initialize(a)
    }

    override fun isValid(value: Map<*, *>?, context: ConstraintValidatorContext): Boolean {
        value ?: return true
        return commonEachValidator!!.isValid(if (validValue) value.values else value.keys, context)
    }
}