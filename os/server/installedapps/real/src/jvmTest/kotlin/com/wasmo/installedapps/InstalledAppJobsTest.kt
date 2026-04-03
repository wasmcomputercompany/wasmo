package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.events.HandleJobEvent
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

class InstalledAppJobsTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(
      publishedApp = app,
    )
    val recipesApp = installedApp.load() as RecipesApp
    val jobQueue = recipesApp.platform.jobQueueFactory.get("shopping-list")
    jobQueue.enqueue("flour".encodeUtf8())

    assertThat(tester.eventListener.receive<HandleJobEvent>())
      .isEqualTo(
        HandleJobEvent(
          appSlug = app.slug,
          queueName = "shopping-list",
          job = "flour".encodeUtf8(),
        ),
      )
  }
}
