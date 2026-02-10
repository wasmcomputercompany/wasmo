package com.wasmo.apps

import com.wasmo.BadRequestException
import com.wasmo.ContentType
import com.wasmo.HttpClient
import com.wasmo.HttpRequest
import com.wasmo.api.AppManifest
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.ByteString

class AppLoader(
  private val json: Json,
  private val httpClient: HttpClient,
) {
  suspend fun loadManifest(manifestUrl: String): AppManifest {
    val httpUrl = manifestUrl.toHttpUrlOrNull()
      ?: throw BadRequestException("unexpected manifestUrl")

    val manifestResponse = httpClient.execute(
      HttpRequest(
        method = "GET",
        url = httpUrl,
      ),
    )

    if (!manifestResponse.isSuccessful) {
      throw BadRequestException("failed to fetch manifest")
    }
    if (manifestResponse.contentType != ContentType.Json) {
      throw BadRequestException("expected ${ContentType.Json} for manifest content-type")
    }

    return try {
      json.decodeFromString<AppManifest>(manifestResponse.body?.utf8() ?: "")
    } catch (_: IllegalArgumentException) {
      throw BadRequestException("failed to decode manifest")
    }
  }

  suspend fun loadWasm(manifest: AppManifest): ByteString {
    val canonicalUrl = manifest.canonicalUrl?.toHttpUrlOrNull()
    val wasmUrl = canonicalUrl?.resolve(manifest.wasmUrl)
      ?: throw BadRequestException("unexpected wasmUrl")

    val wasmResponse = httpClient.execute(
      HttpRequest(
        method = "GET",
        url = wasmUrl,
      ),
    )

    if (!wasmResponse.isSuccessful) {
      throw BadRequestException("failed to fetch wasm")
    }

    return wasmResponse.body ?: ByteString.EMPTY
  }
}
