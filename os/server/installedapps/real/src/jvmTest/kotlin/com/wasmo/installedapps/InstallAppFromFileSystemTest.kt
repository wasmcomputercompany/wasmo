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
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.Issue
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType

class InstallAppFromFileSystemTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val publishedApp = RecipesApp.PublishedApp.withFileSystemWasmoFileAddress()
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
          contentType = "text/html".toMediaType(),
          body = ResponseBodySnapshot("Welcome to the recipes app"),
        ),
      )
  }

  @Test
  @Ignore("we don't yet validate resources at install time")
  fun resourceIsAbsentAtInstallTime() = runTest {
    val publishedApp = RecipesApp.PublishedApp.withFileSystemWasmoFileAddress()
    tester.publishApp(publishedApp)

    val basePath = (publishedApp.wasmoFileAddress as WasmoFileAddress.FileSystem).path
    tester.fileSystem.delete(basePath / "index.html", mustExist = true)

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
          path = (basePath / "index.html").toString(),
          message = "???",
        )
      )
  }

  @Test
  fun resourceIsAbsentAtFetchTime() = runTest {
    val publishedApp = RecipesApp.PublishedApp.withFileSystemWasmoFileAddress()
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    val basePath = (publishedApp.wasmoFileAddress as WasmoFileAddress.FileSystem).path
    tester.fileSystem.delete(basePath / "index.html", mustExist = true)

    assertFailsWith<NotFoundUserException> {
      client.call().callApp(
        url = installedApp.url.resolve("/")!!,
      )
    }
  }

  private fun PublishedApp.withFileSystemWasmoFileAddress(): PublishedApp = copy(
    wasmoFileAddress = WasmoFileAddress.FileSystem(
      tester.testDirectory / "$slug.wasmo",
    ),
  )
}
