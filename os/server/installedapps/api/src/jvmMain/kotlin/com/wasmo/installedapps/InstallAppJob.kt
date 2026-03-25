package com.wasmo.installedapps

import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.identifiers.ComputerId
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val computerId: ComputerId,
  val appManifestAddress: AppManifestAddress,
)
