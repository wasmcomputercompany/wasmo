package com.wasmo.api.routes

import com.wasmo.framework.Url
import com.wasmo.framework.decodeUrl
import kotlinx.serialization.Serializable

@Serializable
data class RoutingContext(
  val rootUrl: String,
) {
  val root: Url
    get() = rootUrl.decodeUrl()
}
