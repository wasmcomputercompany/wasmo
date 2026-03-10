package com.wasmo.testing.installedapp

import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.PublishedApp
import okhttp3.HttpUrl

/**
 * Tests an app installed on a specific computer.
 */
class InstalledAppTester(
  val deployment: Deployment,
  val publishedApp: PublishedApp,
  val computerSlug: ComputerSlug,
  val slug: AppSlug,
) {
  val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug-$computerSlug.${deployment.baseUrl.host}")
      .build()
  val iconUrl: HttpUrl
    get() = url.resolve("/maskable-icon.svg")!!
}
