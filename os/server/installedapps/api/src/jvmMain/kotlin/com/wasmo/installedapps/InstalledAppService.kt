package com.wasmo.installedapps

import com.wasmo.identifiers.AppSlug
import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl
import wasmo.app.Platform

interface InstalledAppService {
  val slug: AppSlug
  val manifest: AppManifest
  val url: HttpUrl
  val maskableIconUrl: HttpUrl
  val httpService: InstalledAppHttpService
  val platform: Platform
}
