package com.wasmo.testing.computer

import com.wasmo.api.InstallAppRequest
import com.wasmo.computers.ComputerScope
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.WasmoArtifactServer
import com.wasmo.testing.apps.TestApp
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.installedapp.InstalledAppTester
import com.wasmo.testing.installedapp.InstalledAppTesterGraph
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Tests a computer belonging to a single user.
 */
@Inject
@SingleIn(ComputerScope::class)
class ComputerTester(
  private val wasmoArtifactServer: WasmoArtifactServer,
  private val installedAppTesterGraphFactory: InstalledAppTesterGraph.Factory,
  private val client: ClientTester,
  val slug: ComputerSlug,
) {
  suspend fun installApp(testApp: TestApp): InstalledAppTester {
    wasmoArtifactServer.apps += testApp

    client.call().installApp(
      computerSlug = slug,
      request = InstallAppRequest(
        manifestUrl = testApp.manifestUrl.toString(),
      ),
    )

    val graph = installedAppTesterGraphFactory.create(testApp)
    return graph.installedAppTester
  }
}

