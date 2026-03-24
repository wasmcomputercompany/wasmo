package com.wasmo.computers

import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.computers.AppManifestAddress.Companion.toAppManifestAddress
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.Launcher
import com.wasmo.packaging.TargetSdk1
import com.wasmo.packaging.WasmoToml
import okio.FileSystem
import okio.Path.Companion.toPath

class AppCatalog(
  val entries: List<Entry>,
) {
  data class Entry(
    val appManifestAddress: AppManifestAddress,
    val manifest: AppManifest,
  ) {
    companion object {
      operator fun invoke(
        slug: AppSlug,
        label: String,
      ) = Entry(
        appManifestAddress = "http://wasmo.localhost:8080/$slug/$slug.wasmo.toml"
          .toAppManifestAddress(),
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
    appManifestAddress = "http://wasmo.localhost:8080/$slug/$slug.wasmo.toml"
      .toAppManifestAddress(),
    manifest = manifest,
  )
}
