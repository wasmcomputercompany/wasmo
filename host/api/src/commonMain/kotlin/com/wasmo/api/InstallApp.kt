package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class InstallAppRequest(
  val manifestUrl: String,
)

@Serializable
data class InstallAppResponse(
  val url: String,
)
