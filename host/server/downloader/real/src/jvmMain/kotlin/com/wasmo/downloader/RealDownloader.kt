package com.wasmo.downloader

import okio.ByteString
import wasmo.downloader.Downloader
import wasmo.downloader.TransferRequest
import wasmo.downloader.TransferResponse
import wasmo.http.HttpService
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

class RealDownloader(
  val httpService: HttpService,
  val objectStore: ObjectStore,
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
