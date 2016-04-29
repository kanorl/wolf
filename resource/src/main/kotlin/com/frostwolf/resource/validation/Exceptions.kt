package com.frostwolf.resource.validation

import com.frostwolf.common.lang.IgnoreStackTraceException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

class ResourceInvalidException : IgnoreStackTraceException {
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

class DuplicateResourceException(val msg: String) : IgnoreStackTraceException(msg) {
    constructor(clazz: Class<*>, duplicate: Collection<Int>) : this("Duplicate id$duplicate in ${clazz.simpleName}")
}