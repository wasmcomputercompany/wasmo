package com.wasmo.installedapps

import com.wasmo.identifiers.AppManifestAddress
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val appManifestAddress: AppManifestAddress,
)
