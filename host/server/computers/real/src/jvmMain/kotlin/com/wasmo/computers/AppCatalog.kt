package com.wasmo.computers

import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import com.wasmo.packaging.WasmoToml
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path.Companion.toPath

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
        manifestUrl = "http://wasmo.localhost:8080/$slug/$slug.wasmo.toml".toHttpUrl(),
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

fun loadDefaultAppCatalogFromResources(): AppCatalog {
  val apps = listOf(
    AppSlug("files"),
    AppSlug("library"),
    AppSlug("music"),
    AppSlug("photos"),
    AppSlug("pink"),
    AppSlug("recipes"),
    AppSlug("smart"),
    AppSlug("snake"),
    AppSlug("writer"),
    AppSlug("zap"),
  )
  return AppCatalog(
    entries = apps.map { loadAppCatalogEntryFromResource(it) },
  )
}

fun loadAppCatalogEntryFromResource(slug: AppSlug): Entry {
  val manifest = FileSystem.RESOURCES.read("/static/$slug/$slug.wasmo.toml".toPath()) {
    WasmoToml.decodeFromString(
      AppManifest.serializer(),
      readUtf8(),
    )
  }
  return Entry(
    manifestUrl = "http://wasmo.localhost:8080/$slug/$slug.wasmo.toml".toHttpUrl(),
    manifest = manifest,
  )
}
