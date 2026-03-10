package com.wasmo.testing.computer

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.installedapp.InstalledAppTester

/**
 * Tests a computer belonging to a single user.
 */
class ComputerTester(
  private val deployment: Deployment,
  private val client: ClientTester,
  val slug: ComputerSlug,
) {
  suspend fun installApp(publishedApp: PublishedApp): InstalledAppTester {
    client.call().installApp(
      computerSlug = slug,
      request = InstallAppRequest(
        manifestUrl = publishedApp.manifestUrl.toString(),
      ),
    )

    return getApp(publishedApp)
  }

  fun getApp(publishedApp: PublishedApp) = InstalledAppTester(
    deployment = deployment,
    publishedApp = publishedApp,
    computerSlug = slug,
    slug = AppSlug(publishedApp.manifest.slug),
  )

  fun homePage() = client.call().hostPage(ComputerHomeRoute(slug))
}
