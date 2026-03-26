package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.ComputerListItem
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.MusicApp
import com.wasmo.testing.apps.SnakeApp
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class CreateComputerActionTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val client = tester.newClient()
    val computerSlug = ComputerSlug("jesse99")
    val createComputerResponse = client.call().createComputerSpec(
      request = CreateComputerSpecRequest(
        computerSpecToken = "computerspectoken00000001",
        slug = computerSlug,
      ),
    )
    val checkoutSessionId = client.paymentsService.completePayment(
      createComputerResponse.body.checkoutSessionClientSecret,
    )
    val afterCheckoutResponse = client.call().afterCheckout(checkoutSessionId)

    assertThat(afterCheckoutResponse.header("Location")).isEqualTo(
      "https://jesse99.wasmo.com/",
    )

    val computerListPage = client.call().hostPage(ComputerListRoute)
    assertThat(computerListPage.computerListSnapshot?.items)
      .isNotNull()
      .containsExactly(ComputerListItem(computerSlug))

    // Apps from TestAppCatalog are installed by default.
    val computer = client.getComputer(computerSlug)
    computer.jobQueueTester.awaitIdle() // TODO: serve InstallAppJobs while waiting.

    val installedMusicApp = computer.getApp(MusicApp.PublishedApp)
    val installedSnakeApp = computer.getApp(SnakeApp.PublishedApp)

    val computerHostPage = client.call().hostPage(ComputerHomeRoute(computerSlug))
    assertThat(computerHostPage.computerSnapshot?.slug).isEqualTo(computerSlug)
    assertThat(computerHostPage.computerSnapshot?.apps)
      .isNotNull()
      .containsExactly(
        InstalledAppSnapshot(
          slug = installedMusicApp.slug,
          launcherLabel = installedMusicApp.publishedApp.appManifest.launcher!!.label!!,
          maskableIconUrl = installedMusicApp.iconUrl.toString(),
        ),
        InstalledAppSnapshot(
          slug = installedSnakeApp.slug,
          launcherLabel = installedSnakeApp.publishedApp.appManifest.launcher!!.label!!,
          maskableIconUrl = installedSnakeApp.iconUrl.toString(),
        ),
      )
  }
}
