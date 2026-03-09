package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledApp
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.events.AppInstallEvent
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath

class InstallAppActionTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val client = tester.newClient()
    val computer = client.createComputer()
    val app = computer.installApp(RecipesApp)

    tester.jobQueueTester.awaitIdle()

    assertThat(
      tester.fileSystem.read("/${computer.slug}/${app.slug}/resources/v1/app.wasm".toPath()) {
        readByteString()
      },
    ).isEqualTo(app.testApp.wasm)

    val computerHostPage = client.call().hostPage(ComputerHomeRoute(computer.slug))
    assertThat(computerHostPage.computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledApp(
          slug = app.slug,
          launcherLabel = app.testApp.launcherLabel,
          maskableIconUrl = "/assets/launcher/sample-folder.svg", // TODO
          installScheduledAt = tester.clock.now(),
        ),
      )

    assertThat(tester.eventListener.takeEvent())
      .isEqualTo(
        AppInstallEvent(
          computerSlug = computer.slug,
          appSlug = app.slug,
        ),
      )
  }
}
