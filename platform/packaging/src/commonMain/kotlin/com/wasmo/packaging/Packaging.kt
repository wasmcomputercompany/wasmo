package com.wasmo.packaging

import kotlinx.serialization.Serializable

@Serializable
data class AppManifest(
  val target: String,
  val version: Long,
  val external_resource: List<ExternalResource> = listOf(),
  val launcher: Launcher? = null,
)

@Serializable
data class ExternalResource(
  val from: String,
  val to: String,
  val include: List<String> = listOf(),
)

@Serializable
data class Launcher(
  val label: String? = null,
  val maskable_icon_path: String? = null,
  val home_path: String? = null,
)
