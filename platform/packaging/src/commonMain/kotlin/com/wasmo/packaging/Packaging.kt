package com.wasmo.packaging

import kotlinx.serialization.Serializable

@Serializable
data class AppManifest(
  val target: String,
  val version: Long,
  val slug: String,
  val base_url: String? = null,
  val resource: List<Resource> = listOf(),
  val route: List<Route> = listOf(),
  val launcher: Launcher? = null,
)

@Serializable
data class Resource(
  val url: String,
  val resource_path: String? = null,
  val content_type: String? = null,
  val unzip: Boolean? = null,
  val sha256: String? = null,
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
