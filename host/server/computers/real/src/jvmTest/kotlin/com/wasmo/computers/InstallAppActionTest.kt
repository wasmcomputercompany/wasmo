package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerSlug
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstalledApp
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.testing.ServiceTester
import com.wasmo.testing.WasmoArtifactServer
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath

class InstallAppActionTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val client = tester.newClient()
    val computerSlug = ComputerSlug("jesse124")
    val computer = client.createComputer(computerSlug)
    val wasm = "XXXX".encodeUtf8()
    val helloApp = WasmoArtifactServer.App(
      slug = AppSlug("hello"),
      launcherLabel = "Hello World",
      version = 1L,
      wasm = wasm,
    )
    tester.wasmoArtifactServer.apps += helloApp

    val installAppResponse = client.call().installApp(
      computerSlug = computer.slug,
      request = InstallAppRequest(
        manifestUrl = tester.deployment.baseUrl.resolve(helloApp.manifestPath)!!.toString(),
      ),
    )

    tester.jobQueueTester.awaitIdle()

    assertThat(
      tester.fileSystem.read("/jesse124/hello/resources/v1/app.wasm".toPath()) {
        readByteString()
      },
    ).isEqualTo(wasm)

    val computerHostPage = client.call().hostPage(ComputerHomeRoute(computerSlug))
    assertThat(computerHostPage.computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledApp(
          slug = AppSlug("hello"),
          launcherLabel = "Hello World",
          maskableIconUrl = "/assets/launcher/sample-folder.svg", // TODO
          installScheduledAt = tester.clock.now(),
        ),
      )
  }
}
