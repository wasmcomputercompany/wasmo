package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.events.AfterInstallEvent
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class InstallAppLifecycleTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  @Ignore("not implemented yet")
  fun happyPath() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    computer.installApp(
      publishedApp = app,
    )

    assertThat(tester.testEventQueue.receive())
      .isEqualTo(
        AfterInstallEvent(
          appSlug = app.slug,
          oldVersion = 0L,
          newVersion = 1L,
        ),
      )
  }
}
