package com.wasmo.wallpapers

import kotlin.time.Instant

data class Wallpaper(
  val title: String,
  val url: String,
  val filename: String,
  val css: String,
  val license: License,
  val photographer: String,
  val date: Instant,
  val location: String? = null,
)

/** We need to be able to widely redistribute all bundled wallpapers. */
enum class License {
  PublicDomain,
}
