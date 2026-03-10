package com.wasmo.computers

import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class AppCatalog(
  val entries: List<Entry>,
) {
  data class Entry(
    val manifestUrl: HttpUrl,
    val manifest: AppManifest,
  ) {
    companion object {
      operator fun invoke(
        slug: AppSlug,
        label: String,
      ) = Entry(
        manifestUrl = "http://localhost:8080/$slug/$slug.wasmo.toml".toHttpUrl(),
        manifest = AppManifest(
          target = TargetSdk1,
          version = 1L,
          slug = slug.value,
          launcher = Launcher(
            label = label,
            maskable_icon_path = "/maskable-icon.svg",
          ),
        ),
      )
    }
  }
}

val DefaultAppCatalog = AppCatalog(
  entries = listOf(
    Entry(
      slug = AppSlug("files"),
      label = "Files",
    ),
    Entry(
      slug = AppSlug("library"),
      label = "Library",
    ),
    Entry(
      slug = AppSlug("music"),
      label = "Music",
    ),
    Entry(
      slug = AppSlug("photos"),
      label = "Photos",
    ),
    Entry(
      slug = AppSlug("pink"),
      label = "Pink Journal",
    ),
    Entry(
      slug = AppSlug("recipes"),
      label = "Recipes",
    ),
    Entry(
      slug = AppSlug("smart"),
      label = "Smart Home",
    ),
    Entry(
      slug = AppSlug("snake"),
      label = "Snake",
    ),
    Entry(
      slug = AppSlug("writer"),
      label = "Writer",
    ),
    Entry(
      slug = AppSlug("zap"),
      label = "Zap",
    ),
  ),
)
