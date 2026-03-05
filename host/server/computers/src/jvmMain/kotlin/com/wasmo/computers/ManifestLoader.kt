package com.wasmo.computers

import com.wasmo.api.AppManifest
import com.wasmo.api.WasmoJson
import dev.zacsweers.metro.Inject
import okhttp3.HttpUrl
import wasmo.http.BadRequestException
import wasmo.http.ContentType
import wasmo.http.HttpClient
import wasmo.http.HttpRequest

@Inject
class ManifestLoader(
  private val httpClient: HttpClient,
) {
  suspend fun loadManifest(manifestUrl: HttpUrl): AppManifest {
    val manifestResponse = httpClient.execute(
      HttpRequest(
        method = "GET",
        url = manifestUrl,
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
