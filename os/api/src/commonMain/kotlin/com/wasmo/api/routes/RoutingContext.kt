package com.wasmo.api.routes

import kotlinx.serialization.Serializable

@Serializable
data class RoutingContext(
  val rootUrl: String,
) {
  val root: Url
    get() = rootUrl.decodeUrl()
}
