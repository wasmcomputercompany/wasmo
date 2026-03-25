package com.wasmo.packaging

import kotlinx.serialization.Serializable

@Serializable
data class AppManifest(
  val target: String,
  val version: Long,
  val slug: String,
  val external_resource: List<ExternalResource> = listOf(),
  val route: List<Route> = listOf(),
  val launcher: Launcher? = null,
)

@Serializable
data class ExternalResource(
  val from: String,
  val to: String,
  val include: List<String>,
)

@Serializable
data class Route(
  val path: String,
  val resource_path: String? = null,
  val objects_key: String? = null,
  val access: String? = null,
)

@Serializable
data class Launcher(
  val label: String? = null,
  val maskable_icon_path: String? = null,
)
