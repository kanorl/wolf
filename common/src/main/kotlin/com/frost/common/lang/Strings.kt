package com.frost.common.lang

import org.apache.commons.lang3.SystemUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

fun settingToString(obj: Any, name: String = obj.javaClass.simpleName): String = ToStringBuilder.reflectionToString(obj, object : ToStringStyle() {
    init {
        fieldNameValueSeparator = ": "
        isUseClassName = false
        isUseIdentityHashCode = false
        fieldSeparator = SystemUtils.LINE_SEPARATOR
        isFieldSeparatorAtStart = true
        contentStart = "_______________________________${name}______________________________"
        contentEnd = "\n____________________________________________________________________________\n"
        arrayStart = "["
        arrayEnd = "]"
    }
})

