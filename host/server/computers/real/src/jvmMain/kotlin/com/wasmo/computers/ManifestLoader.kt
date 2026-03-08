package com.wasmo.computers

import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import com.wasmo.packaging.check
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
    if (manifestResponse.contentType != ContentType.Toml) {
      throw BadRequestException("expected ${ContentType.Toml} for manifest content-type")
    }

    val result = try {
      WasmoToml.decodeFromString(
        AppManifest.serializer(),
        manifestResponse.body.utf8(),
      )
    } catch (_: IllegalArgumentException) {
      throw BadRequestException("failed to decode manifest")
    }

    val issues = result.check()
    if (!issues.isEmpty()) {
      throw BadRequestException(issues.joinToString(separator = "\n\n"))
    }

    return result
  }
}
