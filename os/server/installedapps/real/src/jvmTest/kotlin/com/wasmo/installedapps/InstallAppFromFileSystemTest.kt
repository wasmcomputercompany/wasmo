package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.Issue
import com.wasmo.issues.Severity
import com.wasmo.packaging.ExternalResource
import com.wasmo.testing.apps.PublishedApp
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
    val publishedApp = tester.sampleApps.recipes.publishedApp
      .withFileSystemWasmoFileAddress()
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = "Recipes",
          maskableIconUrl = "https://recipes-${computer.slug}.wasmo.com/maskable-icon.svg",
          homeUrl = "https://recipes-${computer.slug}.wasmo.com/home",
        ),
      )

    assertThat(tester.eventListener.receive<InstallAppEvent>())
      .isEqualTo(
        InstallAppEvent(
          computerSlug = computer.slug,
          appSlug = installedApp.slug,
        ),
      )

    assertThat(installedApp.call("/"))
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
      to = "/www/media",
      include = listOf("*.svg"),
    )
    val originalApp = tester.sampleApps.recipes.publishedApp
    val publishedApp = originalApp
      .withFileSystemWasmoFileAddress()
      .copy(
        appManifest = originalApp.appManifest.copy(
          external_resource = listOf(
            externalResource,
          ),
        ),
        resources = originalApp.resources + mapOf(
          externalResourcePath.toString() to "I am an SVG".encodeUtf8(),
        ),
      )
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    assertThat(installedApp.call("/media/logo.svg"))
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
    val originalApp = tester.sampleApps.recipes.publishedApp
    val publishedApp = originalApp.withFileSystemWasmoFileAddress()
      .copy(
        appManifest = originalApp.appManifest.copy(
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
          launcherLabel = "Recipes",
          maskableIconUrl = "https://recipes-${computer.slug}.wasmo.com/maskable-icon.svg",
          homeUrl = "https://recipes-${computer.slug}.wasmo.com/home",
        ),
      )

    assertThat(tester.eventListener.receive<InstallAppEvent>().issues)
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
    val publishedApp = tester.sampleApps.recipes.publishedApp
      .withFileSystemWasmoFileAddress()
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    val basePath = (publishedApp.wasmoFileAddress as WasmoFileAddress.FileSystem).path
    tester.fileSystem.delete(basePath / "www" / "index.html", mustExist = true)

    assertFailsWith<NotFoundUserException> {
      installedApp.call("/")
    }
  }

  @Test
  fun updateManifestInPlace() = runTest {
    val publishedApp = tester.sampleApps.recipes.publishedApp
      .withFileSystemWasmoFileAddress()
    tester.publishApp(publishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(publishedApp)

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = "Recipes",
          maskableIconUrl = "https://recipes-${computer.slug}.wasmo.com/maskable-icon.svg",
          homeUrl = "https://recipes-${computer.slug}.wasmo.com/home",
        ),
      )

    tester.publishApp(
      publishedApp.copy(
        appManifest = publishedApp.appManifest.copy(
          launcher = publishedApp.appManifest.launcher!!.copy(
            label = "Recipes! Updated!",
          ),
        ),
      ),
    )

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = "Recipes! Updated!",
          maskableIconUrl = "https://recipes-${computer.slug}.wasmo.com/maskable-icon.svg",
          homeUrl = "https://recipes-${computer.slug}.wasmo.com/home",
        ),
      )
  }

  private fun PublishedApp.withFileSystemWasmoFileAddress(): PublishedApp = copy(
    wasmoFileAddress = WasmoFileAddress.FileSystem(
      tester.testDirectory / "$slug.wasmo",
    ),
  )
}
