package com.frost.resource.validation

import com.frost.resource.Resource
import cz.jirutka.validator.collection.CommonEachValidator
import cz.jirutka.validator.collection.constraints.EachConstraint
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.reflect.KClass

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
@MapValueConstraint
annotation class ValuesMin(val value: Long, val message: String = "{javax.validation.constraints.MapValue.message}{javax.validation.constraints.Min.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = Max::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
@MapValueConstraint
annotation class ValuesMax(val value: Long, val message: String = "{javax.validation.constraints.MapValue.message}{javax.validation.constraints.Max.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@EachConstraint(validateAs = ReferTo::class)
@Constraint(validatedBy = arrayOf(MapKeyValueValidator::class))
@MapValueConstraint
annotation class ValuesReferTo(val value: KClass<out Resource>, val message: String = "{javax.validation.constraints.MapValue.message}{javax.validation.constraints.ReferTo.message}", val groups: Array<KClass<*>> = arrayOf(), val payload: Array<KClass<in Payload>> = arrayOf())
