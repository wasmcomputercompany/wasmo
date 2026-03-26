package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.events.InstallAppEvent
import com.wasmo.framework.Response
import com.wasmo.issues.Issue
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
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
  fun wasmoFileIsAbsent() = runTest {
    val app = RecipesApp.PublishedApp
    val client = tester.newClient()
    val computer = client.createComputer()
    computer.installApp(app)

    assertThat(tester.eventListener.takeEvent().issues)
      .containsExactly(
        Issue(
          url = app.wasmoFileAddress.toString(),
          message = "HTTP request failed: 404",
        ),
      )
  }

  @Test
  fun wasmoFileIsMalformedZip() = runTest {
    val app = RecipesApp.PublishedApp
    tester.fakeHttpClient += FakeHttpService.Handler {
      HttpResponse(
        body = "this is not a .wasmo file".encodeUtf8(),
      )
    }

    val client = tester.newClient()
    val computer = client.createComputer()
    computer.installApp(app)

    assertThat(tester.eventListener.takeEvent().issues)
      .containsExactly(
        Issue(
          url = app.wasmoFileAddress.toString(),
          message = "No wasmo-manifest.toml file in .wasmo archive",
        ),
      )
  }
}
