package com.wasmo.testing

import com.wasmo.computers.AppCatalog
import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.identifiers.AppSlug

val TestAppCatalog = AppCatalog(
  entries = listOf(
    Entry(
      slug = AppSlug("music"),
      launcherLabel = "Music",
      maskableIconUrl = "/assets/launcher/sample-headphones.svg",
    ),
    Entry(
      slug = AppSlug("snake"),
      launcherLabel = "Snake",
      maskableIconUrl = "/assets/launcher/sample-snake.svg",
    ),
  ),
)
