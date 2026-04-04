package com.wasmo.testing.computer

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.events.TestEventListener
import com.wasmo.testing.installedapp.InstalledAppTester
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

/**
 * Tests a computer belonging to a single user.
 */
@AssistedInject
class ComputerTester private constructor(
  private val installedAppTesterFactory: InstalledAppTester.Factory,
  private val eventListener: TestEventListener,
  @Assisted private val clientAuthenticator: ClientAuthenticator,
  @Assisted private val client: ClientTester,
  @Assisted val slug: ComputerSlug,
) {
  /**
   * @param waitForInstall true to wait for the async app install job to complete before returning.
   */
  suspend fun installApp(
    publishedApp: PublishedApp,
    waitForInstall: Boolean = true,
  ): InstalledAppTester {
    client.call().installApp(
      computerSlug = slug,
      request = InstallAppRequest(
        appManifestAddress = publishedApp.wasmoFileAddress.toString(),
        appSlug = publishedApp.slug,
      ),
    )

    if (waitForInstall) {
      eventListener.awaitIdle()
    }

    return getApp(publishedApp)
  }

  fun getApp(publishedApp: PublishedApp) = installedAppTesterFactory.create(
    clientAuthenticator = clientAuthenticator,
    publishedApp = publishedApp,
    computerSlug = slug,
  )

  fun homePage() = client.call().osPage(ComputerHomeRoute(slug))

  @AssistedFactory
  interface Factory {
    fun create(
      clientAuthenticator: ClientAuthenticator,
      client: ClientTester,
      slug: ComputerSlug,
    ): ComputerTester
  }
}
