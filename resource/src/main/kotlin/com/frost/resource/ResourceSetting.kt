package com.frost.resource

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "resource")
class ResourceSetting {
    var reader = "json"
    var path = ""
}