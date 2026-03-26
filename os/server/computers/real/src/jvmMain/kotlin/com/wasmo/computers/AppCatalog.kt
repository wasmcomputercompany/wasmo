package com.wasmo.computers

import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.identifiers.AppManifestAddress.Companion.toAppManifestAddress
import com.wasmo.identifiers.AppSlug

class AppCatalog(
  val entries: List<Entry>,
) {
  data class Entry(
    val appManifestAddress: AppManifestAddress,
    val slug: AppSlug,
  ) {
    companion object {
      operator fun invoke(slug: AppSlug) = resourceAppAsCatalogEntry(slug)
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
    entries = apps.map { resourceAppAsCatalogEntry(it) },
  )
}

fun resourceAppAsCatalogEntry(slug: AppSlug): Entry {
  return Entry(
    appManifestAddress = "http://wasmo.localhost:8080/$slug/$slug.wasmo.toml"
      .toAppManifestAddress(),
    slug = slug,
  )
}
