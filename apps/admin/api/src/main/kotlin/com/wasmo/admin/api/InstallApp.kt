package com.wasmo.admin.api

import kotlinx.serialization.Serializable

@Serializable
data class InstallAppRequest(
  val manifest: AppManifest,
)

@Serializable
data class InstallAppResponse(
  val url: String,
)
