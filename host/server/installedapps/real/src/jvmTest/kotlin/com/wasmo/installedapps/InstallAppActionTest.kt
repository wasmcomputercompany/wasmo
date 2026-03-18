package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.wasmo.api.InstallIncompleteReason
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.StateUserException
import com.wasmo.packaging.Resource
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.service.ServiceTester
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import wasmo.http.FakeHttpService
import wasmo.http.Header
import wasmo.http.HttpResponse

class InstallAppActionTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    tester.publishApp(RecipesApp.PublishedApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installScheduledAt = tester.clock.now
    val installedApp = computer.installApp(
      publishedApp = RecipesApp.PublishedApp,
      waitForInstall = false,
    )

    tester.clock.now += 30.minutes
    val installCompletedAt = tester.clock.now
    tester.jobQueueTester.awaitIdle()

    val appWasmPath = "/${computer.slug}/${installedApp.slug}/resources/v1/app.wasm".toPath()
    assertThat(
      tester.fileSystem.read(appWasmPath) {
        readByteString()
      },
    ).isEqualTo(installedApp.publishedApp.wasm)

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = installedApp.publishedApp.manifest.launcher!!.label!!,
          maskableIconUrl = installedApp.iconUrl.toString(),
          installScheduledAt = installScheduledAt,
          installCompletedAt = installCompletedAt,
        ),
      )

    assertThat(tester.eventListener.takeEvent())
      .isEqualTo(
        InstallAppEvent(
          computerSlug = computer.slug,
          appSlug = installedApp.slug,
        ),
      )
  }

  @Test
  fun manifestIsAbsent() = runTest {
    val client = tester.newClient()
    val computer = client.createComputer()
    assertThat(
      assertFailsWith<StateUserException> {
        computer.installApp(RecipesApp.PublishedApp)
      },
    ).hasMessage("failed to fetch manifest: HTTP 404")
  }

  @Test
  fun manifestIsMalformedToml() = runTest {
    tester.fakeHttpClient += FakeHttpService.Handler { request ->
      HttpResponse(
        headers = listOf(Header("Content-Type", ContentTypes.ApplicationToml.toString())),
        body = "[[[this is not valid toml]]]".encodeUtf8(),
      )
    }

    val client = tester.newClient()
    val computer = client.createComputer()
    assertThat(
      assertFailsWith<StateUserException> {
        computer.installApp(RecipesApp.PublishedApp)
      },
    ).messageContains("failed to decode manifest")
  }

  /** Publish an app whose served resources are different from its manifest resources. */
  @Test
  fun resource404s() = runTest {
    val brokenApp = RecipesApp.PublishedApp.copy(
      servedResources = mapOf(),
    )
    tester.publishApp(brokenApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(brokenApp)

    assertFailsWith<FileNotFoundException> {
      tester.fileSystem.list(
        "/${computer.slug}/${installedApp.slug}/resources/v1/".toPath(),
      )
    }

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledAppSnapshot(
          slug = installedApp.slug,
          launcherLabel = installedApp.publishedApp.manifest.launcher!!.label!!,
          maskableIconUrl = installedApp.iconUrl.toString(),
          installScheduledAt = tester.clock.now(),
          installIncompleteReason = InstallIncompleteReason.SourceUnavailable,
        ),
      )

    assertThat(tester.eventListener.takeEvent().exception)
      .isNotNull()
      .hasMessage("failed to fetch https://example.com/recipes/v1/app.wasm: HTTP 404")
  }

  @Test
  fun resourceGoodSha256() = runTest {
    val pancakesUrl = "https://example.com/pancakes.txt".toHttpUrl()
    val pancakesData = "this pancakes recipe has a SHA-256 signature".encodeUtf8()
    val original = RecipesApp.PublishedApp
    val app = original.copy(
      manifest = original.manifest.copy(
        resource = original.manifest.resource + listOf(
          Resource(
            url = pancakesUrl.toString(),
            sha256 = pancakesData.sha256().hex(),
          ),
        ),
      ),
      servedResources = original.servedResources + mapOf(
        pancakesUrl to pancakesData,
      ),
    )

    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    val pancakesPath = "/${computer.slug}/${installedApp.slug}/resources/v1/pancakes.txt".toPath()
    assertThat(
      tester.fileSystem.read(pancakesPath) {
        readByteString()
      },
    ).isEqualTo(pancakesData)
  }

  @Test
  fun resourceBadSha256() = runTest {
    val pancakesUrl = "https://example.com/pancakes.txt".toHttpUrl()
    val pancakesData1 = "this pancakes recipe has a SHA-256 signature".encodeUtf8()
    val pancakesData2 = "this pancakes recipe has a SHA-256 signature!".encodeUtf8()
    val original = RecipesApp.PublishedApp
    val app = original.copy(
      manifest = original.manifest.copy(
        resource = original.manifest.resource + listOf(
          Resource(
            url = pancakesUrl.toString(),
            sha256 = pancakesData1.sha256().hex(),
          ),
        ),
      ),
      servedResources = original.servedResources + mapOf(
        pancakesUrl to pancakesData2,
      ),
    )

    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

    val pancakesPath = "/${computer.slug}/${installedApp.slug}/resources/v1/pancakes.txt".toPath()
    assertFailsWith<FileNotFoundException> {
      tester.fileSystem.read(pancakesPath) {
        readByteString()
      }
    }

    assertThat(tester.eventListener.takeEvent().exception)
      .isNotNull()
      .hasMessage("response body data for $pancakesUrl didn't match sha256 from manifest")
  }
}
