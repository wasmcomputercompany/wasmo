package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.computers.AppManifestAddress
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.Response
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class InstallAppFromFileSystemTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val publishedApp = RecipesApp.PublishedApp.copy(
      appManifestAddress = AppManifestAddress.FileSystem(
        tester.testDirectory / "recipes.wasmo.toml",
      ),
    )
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(
      publishedApp = publishedApp,
    )

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = installedApp.publishedApp.manifest.launcher!!.label!!,
          maskableIconUrl = installedApp.iconUrl.toString(),
          installScheduledAt = tester.clock.now,
          installCompletedAt = tester.clock.now,
        ),
      )

    assertThat(tester.eventListener.takeEvent())
      .isEqualTo(
        InstallAppEvent(
          computerSlug = computer.slug,
          appSlug = installedApp.slug,
        ),
      )

    val response = client.call().callApp(
      url = installedApp.url.resolve("/")!!,
    )
    assertThat(response)
      .isEqualTo(
        Response(
          body = ResponseBodySnapshot("Welcome to the recipes app"),
        ),
      )
  }
}
