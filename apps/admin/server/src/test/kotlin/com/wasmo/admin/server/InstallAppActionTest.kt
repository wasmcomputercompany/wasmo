package com.wasmo.admin.server

import com.wasmo.ContentType
import com.wasmo.FakeHttpClient
import com.wasmo.Header
import com.wasmo.HttpResponse
import com.wasmo.admin.api.AdminJson
import com.wasmo.admin.api.InstallAppRequest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8

class InstallAppActionTest {
  private val baseUrl = "https://example.com/".toHttpUrl()
  private val server = WasmoArtifactServer(AdminJson)
  private val httpClient = FakeHttpClient().apply {
    this += server
  }

  @Test
  fun happyPath() = runTest {
    val action = InstallAppAction(
      appLoader = AppLoader(
        json = AdminJson,
        httpClient = httpClient,
      ),
    )
    val helloApp = WasmoArtifactServer.App(
      name = "hello",
      wasm = "XXXX".encodeUtf8(),
    )
    server.apps += helloApp

    val installAppResponse = action.installApp(
      InstallAppRequest(
        manifestUrl = baseUrl.resolve(helloApp.manifestPath)!!.toString(),
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
