package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.issues.Issue
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class InstallAppFromFileSystemTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val publishedApp = RecipesApp.PublishedApp.withFileSystemAppManifestAddress()
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = installedApp.publishedApp.appManifest.launcher!!.label!!,
          maskableIconUrl = installedApp.iconUrl.toString(),
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

  @Test
  fun resourceIsAbsentAtInstallTime() = runTest {
    val publishedApp = RecipesApp.PublishedApp.withFileSystemAppManifestAddress()
    tester.publishApp(publishedApp)

    val missingResourcePath = tester.testDirectory / "index.html"
    tester.fileSystem.delete(missingResourcePath, mustExist = true)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    assertThat(tester.objectStore.list("${computer.slug}/${installedApp.slug}/resources/v1/"))
      .isEmpty()

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = installedApp.publishedApp.appManifest.launcher!!.label!!,
          maskableIconUrl = installedApp.iconUrl.toString(),
        ),
      )

    assertThat(tester.eventListener.takeEvent().issues)
      .containsExactly(
        Issue(
          path = missingResourcePath.toString(),
          message = "???",
        )
      )
  }

  @Test
  fun resourceIsAbsentAtFetchTime() = runTest {
    val publishedApp = RecipesApp.PublishedApp.withFileSystemAppManifestAddress()
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    val missingResourcePath = tester.testDirectory / "index.html"
    tester.fileSystem.delete(missingResourcePath, mustExist = true)

    assertFailsWith<NotFoundUserException> {
      client.call().callApp(
        url = installedApp.url.resolve("/")!!,
      )
    }
  }

  private fun PublishedApp.withFileSystemAppManifestAddress(): PublishedApp = copy(
    appManifestAddress = AppManifestAddress.FileSystem(
      tester.testDirectory / "${appManifest.slug}.wasmo.toml",
    ),
  )
}
