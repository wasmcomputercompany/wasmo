package com.wasmo.installedapps

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.framework.Response
import com.wasmo.issues.Issue
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.FakeHttpService
import wasmo.http.HttpResponse

class InstalledAppHttpServiceTest {
  @InterceptTest
  val tester = ServiceTester()

  // TODO: test resources/www
  // TODO: test resources/www-public
  // TODO: test object_store/www
  // TODO: test object_store/www-public
  // TODO: test resources precedence
  // TODO: test object store precedence
  // TODO: test total precedence

  @Test
  fun happyPath() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val client = tester.newClient()
    val computer = client.createComputer()
    val installedApp = computer.installApp(
      publishedApp = app,
    )

    val appWasmKey = "${computer.slug}/${installedApp.slug}/resources/v1/app.wasm"
    assertThat(tester.objectStore[appWasmKey]).isEqualTo(installedApp.publishedApp.wasm)

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
}
