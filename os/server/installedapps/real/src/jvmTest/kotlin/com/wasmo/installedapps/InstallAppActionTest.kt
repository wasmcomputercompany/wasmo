package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.Response
import com.wasmo.framework.StateUserException
import com.wasmo.issues.Issue
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.FakeHttpService
import wasmo.http.HttpResponse

class InstallAppActionTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    val app = RecipesApp.PublishedApp
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(
      publishedApp = app,
    )

    val appWasmKey = "${computer.slug}/${installedApp.slug}/resources/v1/app.wasm"
    assertThat(tester.objectStore[appWasmKey]).isEqualTo(installedApp.publishedApp.wasm)

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
  fun manifestIsAbsent() = runTest {
    val app = RecipesApp.PublishedApp
    val client = tester.newClient()
    val computer = client.createComputer()
    assertThat(
      assertFailsWith<StateUserException> {
        computer.installApp(app)
      },
    ).hasMessage("failed to fetch manifest: HTTP 404")
  }

  @Test
  fun manifestIsMalformedZip() = runTest {
    val app = RecipesApp.PublishedApp
    tester.fakeHttpClient += FakeHttpService.Handler {
      HttpResponse(
        body = "this is not a .wasmo file".encodeUtf8(),
      )
    }

    val client = tester.newClient()
    val computer = client.createComputer()
    assertThat(
      assertFailsWith<StateUserException> {
        computer.installApp(app)
      },
    ).messageContains("failed to decode manifest")
  }

  /** Publish an app whose served resources are different from its manifest resources. */
  @Test
  fun resource404s() = runTest {
    val app = RecipesApp.PublishedApp.copy(
      resources = mapOf(),
    )
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(app)

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
          message = "failed to fetch https://example.com/recipes/v1/app.wasm: HTTP 404",
        ),
      )
  }
}
