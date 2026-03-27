package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.Issue
import com.wasmo.issues.Severity
import com.wasmo.packaging.ExternalResource
import com.wasmo.packaging.Route
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okio.ByteString.Companion.encodeUtf8

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
  fun externalResource() = runTest {
    val externalResourcePath = tester.testDirectory / "external-resources" / "logo.svg"
    val externalResource = ExternalResource(
      from = externalResourcePath.parent!!.toString(),
      to = "/graphics",
      include = listOf("*.svg"),
    )
    val publishedApp = RecipesApp.PublishedApp.withFileSystemWasmoFileAddress()
      .copy(
        appManifest = RecipesApp.PublishedApp.appManifest.copy(
          route = RecipesApp.PublishedApp.appManifest.route + listOf(
            Route(
              path = "/media/**",
              resource_path = "/graphics/**",
            ),
          ),
          external_resource = listOf(
            externalResource,
          ),
        ),
        resources = RecipesApp.PublishedApp.resources + mapOf(
          externalResourcePath.toString() to "I am an SVG".encodeUtf8(),
        ),
      )
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    val response = client.call().callApp(
      url = installedApp.url.resolve("/media/logo.svg")!!,
    )
    assertThat(response)
      .isEqualTo(
        Response(
          contentType = "image/svg+xml".toMediaType(),
          body = ResponseBodySnapshot("I am an SVG"),
        ),
      )
  }

  @Test
  fun externalResourceIsAbsentAtInstallTime() = runTest {
    val externalResourcePath = tester.testDirectory / "no-such-directory" / "logo.svg"
    val externalResource = ExternalResource(
      from = externalResourcePath.toString(),
      to = "/logo.svg",
    )
    val publishedApp = RecipesApp.PublishedApp.withFileSystemWasmoFileAddress()
      .copy(
        appManifest = RecipesApp.PublishedApp.appManifest.copy(
          external_resource = listOf(
            externalResource,
          ),
        ),
      )
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

    assertThat(tester.eventListener.takeEvent().issues)
      .containsExactly(
        Issue(
          message = "No files found",
          path = externalResourcePath.toString(),
          href = "external_resource[0]",
          severity = Severity.Warning,
        ),
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
