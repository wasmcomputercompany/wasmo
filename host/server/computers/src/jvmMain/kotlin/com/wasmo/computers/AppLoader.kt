package com.wasmo.computers

import com.wasmo.api.AppManifest
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import wasmo.downloader.Downloader
import wasmo.downloader.TransferRequest
import wasmo.http.BadRequestException
import wasmo.http.HttpRequest

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
