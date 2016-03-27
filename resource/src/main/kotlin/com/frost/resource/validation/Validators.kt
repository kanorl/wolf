package com.frost.resource.validation

import com.frost.resource.ResourceManager
import cz.jirutka.validator.collection.CommonEachValidator
import org.springframework.beans.factory.annotation.Autowired
import javax.inject.Inject
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Validation
import javax.validation.ValidatorFactory

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
