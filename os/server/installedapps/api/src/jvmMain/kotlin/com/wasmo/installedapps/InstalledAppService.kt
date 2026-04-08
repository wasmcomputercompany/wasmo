package com.wasmo.installedapps

import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl
import wasmo.app.Platform
import wasmo.app.WasmoApp

interface InstalledAppService {
  val slug: AppSlug
  val manifest: AppManifest
  val url: HttpUrl
  val homeUrl: HttpUrl
  val maskableIconUrl: HttpUrl
  val httpService: InstalledAppHttpService
  val platform: Platform

  suspend fun app(): WasmoApp?

  fun snapshot(): InstalledAppSnapshot
}
