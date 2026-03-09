package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.wasmo.FakeHttpClient
import com.wasmo.api.InstalledApp
import com.wasmo.events.AppInstallEvent
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.StateUserException
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.service.ServiceTester
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import wasmo.http.Header
import wasmo.http.HttpResponse

class InstallAppActionTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun happyPath() = runTest {
    tester.publishApp(RecipesApp)

    val client = tester.newClient()
    val computer = client.createComputer()
    val app = computer.installApp(RecipesApp)

    tester.jobQueueTester.awaitIdle()

    assertThat(
      tester.fileSystem.read("/${computer.slug}/${app.slug}/resources/v1/app.wasm".toPath()) {
        readByteString()
      },
    ).isEqualTo(app.publishedApp.wasm)

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledApp(
          slug = app.slug,
          launcherLabel = app.publishedApp.manifest.launcher!!.label!!,
          maskableIconUrl = "/assets/launcher/sample-folder.svg", // TODO
          installScheduledAt = tester.clock.now(),
        ),
      )

    assertThat(tester.eventListener.takeEvent())
      .isEqualTo(
        AppInstallEvent(
          computerSlug = computer.slug,
          appSlug = app.slug,
        ),
      )
  }

  @Test
  fun manifestIsAbsent() = runTest {
    val client = tester.newClient()
    val computer = client.createComputer()
    assertThat(
      assertFailsWith<StateUserException> {
        computer.installApp(RecipesApp)
      },
    ).hasMessage("failed to fetch manifest: HTTP 404")
  }

  @Test
  fun manifestIsMalformedToml() = runTest {
    tester.fakeHttpClient += FakeHttpClient.Handler { request ->
      HttpResponse(
        headers = listOf(Header("Content-Type", ContentTypes.ApplicationToml.toString())),
        body = "[[[this is not valid toml]]]".encodeUtf8(),
      )
    }

    val client = tester.newClient()
    val computer = client.createComputer()
    assertThat(
      assertFailsWith<StateUserException> {
        computer.installApp(RecipesApp)
      },
    ).messageContains("failed to decode manifest")
  }

  @Test
  fun resource404s() = runTest {
    tester.publishApp(
      RecipesApp.copy(resources = mapOf()),
    )

    val client = tester.newClient()
    val computer = client.createComputer()
    val app = computer.installApp(RecipesApp)

    tester.jobQueueTester.awaitIdle()

    assertFailsWith<FileNotFoundException> {
      tester.fileSystem.list(
        "/${computer.slug}/${app.slug}/resources/v1/".toPath(),
      )
    }

    assertThat(computer.homePage().computerSnapshot?.apps)
      .isNotNull()
      .contains(
        InstalledApp(
          slug = app.slug,
          launcherLabel = app.publishedApp.manifest.launcher!!.label!!,
          maskableIconUrl = "/assets/launcher/sample-folder.svg", // TODO
          installScheduledAt = tester.clock.now(),
        ),
      )

    assertThat(tester.eventListener.takeEvent().exception)
      .isNotNull()
      .hasMessage("failed to fetch https://example.com/recipes/v1/app.wasm: HTTP 404")
  }
}
