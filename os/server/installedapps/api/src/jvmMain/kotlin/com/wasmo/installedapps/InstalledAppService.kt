package com.wasmo.installedapps

import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.identifiers.AppSlug
import okhttp3.HttpUrl
import wasmo.app.Platform
import wasmo.app.WasmoApp

interface InstalledAppService {
  val slug: AppSlug
  val appManifestLoader: AppManifestLoader
  val url: HttpUrl
  val httpService: InstalledAppHttpService
  val platform: Platform

  suspend fun app(): WasmoApp?

  suspend fun homeUrl(): HttpUrl

  suspend fun maskableIconUrl(): HttpUrl

  suspend fun snapshot(): InstalledAppSnapshot
}
