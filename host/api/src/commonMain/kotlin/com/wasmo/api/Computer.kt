package com.wasmo.api

import com.wasmo.identifiers.ComputerSlug
import kotlinx.serialization.Serializable

@Serializable
data class ComputerSnapshot(
  val slug: ComputerSlug,
  val apps: List<InstalledApp>,
)

