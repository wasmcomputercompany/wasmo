package com.wasmo.computers

import com.wasmo.api.AppManifest
import com.wasmo.downloader.Downloader
import com.wasmo.downloader.TransferRequest
import com.wasmo.http.BadRequestException
import com.wasmo.http.ContentType
import com.wasmo.http.HttpClient
import com.wasmo.http.HttpRequest
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class AppLoader(
  private val json: Json,
  private val httpClient: HttpClient,
  private val downloader: Downloader,
  private val objectStoreKeyFactory: ObjectStoreKeyFactory,
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

  suspend fun downloadWasm(manifest: AppManifest) {
    val canonicalUrl = manifest.canonicalUrl?.toHttpUrlOrNull()
    val wasmUrl = canonicalUrl?.resolve(manifest.wasmUrl)
      ?: throw BadRequestException("unexpected wasmUrl")

    val transferResponse = downloader.download(
      transferRequest = TransferRequest(
        httpRequest = HttpRequest(
          method = "GET",
          url = wasmUrl,
        ),
        objectStoreKey = objectStoreKeyFactory.wasm(manifest.slug, manifest.version),
      ),
    )

    if (!transferResponse.httpResponse.isSuccessful) {
      throw BadRequestException("failed to fetch wasm")
    }
  }
}
