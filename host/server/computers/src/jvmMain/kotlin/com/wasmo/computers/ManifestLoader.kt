package com.wasmo.computers

import com.wasmo.api.AppManifest
import com.wasmo.api.WasmoJson
import com.wasmo.http.BadRequestException
import com.wasmo.http.ContentType
import com.wasmo.http.HttpClient
import com.wasmo.http.HttpRequest
import dev.zacsweers.metro.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@Inject
class ManifestLoader(
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
      WasmoJson.decodeFromString<AppManifest>(manifestResponse.body?.utf8() ?: "")
    } catch (_: IllegalArgumentException) {
      throw BadRequestException("failed to decode manifest")
    }
  }
}
