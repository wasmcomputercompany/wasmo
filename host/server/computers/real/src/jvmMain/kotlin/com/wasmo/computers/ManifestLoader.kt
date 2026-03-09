package com.wasmo.computers

import com.wasmo.framework.StateUserException
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import com.wasmo.packaging.check
import dev.zacsweers.metro.Inject
import okhttp3.HttpUrl
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
      throw StateUserException("failed to fetch manifest")
    }
    if (manifestResponse.contentType != ContentType.Toml) {
      throw StateUserException("expected ${ContentType.Toml} for manifest content-type")
    }

    val result = try {
      WasmoToml.decodeFromString(
        AppManifest.serializer(),
        manifestResponse.body.utf8(),
      )
    } catch (e: Throwable) {
      throw StateUserException("failed to decode manifest\n\n${e.message}")
    }

    val issues = result.check()
    if (!issues.isEmpty()) {
      throw StateUserException("invalid manifest\n\n${issues.joinToString(separator = "\n\n")}")
    }

    return result
  }
}
