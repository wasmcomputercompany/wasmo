package com.wasmo.admin.server

import com.wasmo.ContentType
import com.wasmo.Header
import com.wasmo.HttpResponse
import com.wasmo.admin.api.InstallAppRequest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.encodeUtf8

class InstallAppActionTest {
  private lateinit var tester: AdminAppTester

  @BeforeTest
  fun setUp() {
    tester = AdminAppTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() = runTest {
    val action = tester.app.installAppAction()
    val helloApp = WasmoArtifactServer.App(
      slug = "hello",
      displayName = "Hello World",
      wasm = "XXXX".encodeUtf8(),
    )
    tester.wasmoArtifactServer.apps += helloApp

    val installAppResponse = action.installApp(
      InstallAppRequest(
        manifestUrl = tester.baseUrl.resolve(helloApp.manifestPath)!!.toString(),
      ),
    )
  }
}

inline fun <reified T> HttpResponse(
  json: Json,
  code: Int = 200,
  headers: List<Header> = listOf(),
  body: T,
) = HttpResponse(
  code = code,
  headers = headers + Header("Content-Type", ContentType.Json),
  body = json.encodeToString<T>(body).encodeUtf8(),
)
