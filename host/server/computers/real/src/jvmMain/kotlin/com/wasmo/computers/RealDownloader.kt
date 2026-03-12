package com.wasmo.computers

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString
import wasmo.downloader.Downloader
import wasmo.downloader.TransferRequest
import wasmo.downloader.TransferResponse
import wasmo.http.HttpService
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

@Inject
@SingleIn(ComputerScope::class)
class RealDownloader(
  val httpService: HttpService,
  @ForComputer val objectStore: ObjectStore,
) : Downloader {
  override suspend fun download(transferRequest: TransferRequest): TransferResponse {
    val httpResponse = httpService.execute(transferRequest.httpRequest)
    val httpResponseEmptyBody = httpResponse.copy(body = ByteString.EMPTY)
    val body = httpResponse.body

    if (!httpResponse.isSuccessful) {
      return TransferResponse(
        httpResponse = httpResponseEmptyBody,
        etag = null,
      )
    }

    val putObjectResponse = objectStore.put(
      PutObjectRequest(
        key = transferRequest.objectStoreKey,
        value = body,
      ),
    )

    return TransferResponse(
      httpResponse = httpResponseEmptyBody,
      etag = putObjectResponse.etag,
    )
  }
}
