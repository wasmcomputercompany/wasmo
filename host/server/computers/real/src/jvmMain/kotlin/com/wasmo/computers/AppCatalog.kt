package com.wasmo.computers

import com.wasmo.api.AppSlug
import com.wasmo.computers.AppCatalog.Entry

class AppCatalog(
  val entries: List<Entry>,
) {
  data class Entry(
    val slug: AppSlug,
    val launcherLabel: String,
    val maskableIconUrl: String,
  )
}

val DefaultAppCatalog = AppCatalog(
  entries = listOf(
    Entry(
      slug = AppSlug("files"),
      launcherLabel = "Files",
      maskableIconUrl = "/assets/launcher/sample-folder.svg",
    ),
    Entry(
      slug = AppSlug("library"),
      launcherLabel = "Library",
      maskableIconUrl = "/assets/launcher/sample-books.svg",
    ),
    Entry(
      slug = AppSlug("music"),
      launcherLabel = "Music",
      maskableIconUrl = "/assets/launcher/sample-headphones.svg",
    ),
    Entry(
      slug = AppSlug("photos"),
      launcherLabel = "Photos",
      maskableIconUrl = "/assets/launcher/sample-camera.svg",
    ),
    Entry(
      slug = AppSlug("pink"),
      launcherLabel = "Pink Journal",
      maskableIconUrl = "/assets/launcher/sample-flower.svg",
    ),
    Entry(
      slug = AppSlug("recipes"),
      launcherLabel = "Recipes",
      maskableIconUrl = "/assets/launcher/sample-pancakes.svg",
    ),
    Entry(
      slug = AppSlug("smart"),
      launcherLabel = "Smart Home",
      maskableIconUrl = "/assets/launcher/sample-home.svg",
    ),
    Entry(
      slug = AppSlug("snake"),
      launcherLabel = "Snake",
      maskableIconUrl = "/assets/launcher/sample-snake.svg",
    ),
    Entry(
      slug = AppSlug("writer"),
      launcherLabel = "Writer",
      maskableIconUrl = "/assets/launcher/sample-w.svg",
    ),
    Entry(
      slug = AppSlug("zap"),
      launcherLabel = "Zap",
      maskableIconUrl = "/assets/launcher/sample-z.svg",
    ),
  ),
)
