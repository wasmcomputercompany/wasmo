package com.publicobject.wasmcomputer.api

import kotlinx.serialization.Serializable

@Serializable
data class InstallRequest(
  val appManifestUrl: String,
)

@Serializable
data class InstallResponse(
  val installId: String,
)
