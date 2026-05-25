package com.wasmo.computers

import com.wasmo.computers.AppCatalog.Entry
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress
import okhttp3.HttpUrl

class AppCatalog(
  val entries: List<Entry>,
) {
  data class Entry(
    val wasmoFileAddress: WasmoFileAddress,
    val slug: AppSlug,
  ) {
    companion object {
      operator fun invoke(
        baseUrl: HttpUrl,
        slug: AppSlug,
      ) = resourceAppAsCatalogEntry(baseUrl, slug)
    }
  }
}

fun loadDefaultAppCatalogFromResources(baseUrl: HttpUrl): AppCatalog {
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
    entries = apps.map { resourceAppAsCatalogEntry(baseUrl, it) },
  )
}

fun resourceAppAsCatalogEntry(
  baseUrl: HttpUrl,
  slug: AppSlug,
): Entry {
  return Entry(
    wasmoFileAddress = WasmoFileAddress.Http(baseUrl.resolve("/$slug/$slug.wasmo")!!),
    slug = slug,
  )
}
