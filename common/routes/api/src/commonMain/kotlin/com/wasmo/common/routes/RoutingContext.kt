package com.wasmo.common.routes

import kotlinx.serialization.Serializable

@Serializable
data class RoutingContext(
  val rootUrl: String,
  val hasComputers: Boolean,
  val hasInvite: Boolean,
  val isAdmin: Boolean,
) {
  val root: Url
    get() = rootUrl.decodeUrl()
}
