package com.wasmo.computers

import com.wasmo.api.AppManifest
import com.wasmo.downloader.Downloader
import com.wasmo.downloader.TransferRequest
import com.wasmo.http.BadRequestException
import com.wasmo.http.HttpRequest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class AppLoader(
  private val downloader: Downloader,
  private val objectStoreKeyFactory: ObjectStoreKeyFactory,
) {
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
