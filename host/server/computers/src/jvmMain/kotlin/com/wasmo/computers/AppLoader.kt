package com.wasmo.computers

import com.wasmo.api.AppManifest
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl
import wasmo.downloader.Downloader
import wasmo.downloader.TransferRequest
import wasmo.http.BadRequestException
import wasmo.http.HttpRequest

@Inject
@SingleIn(ComputerScope::class)
class AppLoader(
  private val downloader: Downloader,
  private val objectStoreKeyFactory: ObjectStoreKeyFactory,
) {
  suspend fun downloadWasm(manifestUrl: HttpUrl, manifest: AppManifest) {
    val wasmUrl = manifestUrl.resolve(manifest.wasmUrl)
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
