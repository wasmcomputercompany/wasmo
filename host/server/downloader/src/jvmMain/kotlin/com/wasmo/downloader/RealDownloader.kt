package com.wasmo.downloader

import wasmo.downloader.Downloader
import wasmo.downloader.TransferRequest
import wasmo.downloader.TransferResponse
import wasmo.http.HttpClient
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest

class RealDownloader(
  val httpClient: HttpClient,
  val objectStore: ObjectStore,
) : Downloader {
  override suspend fun download(transferRequest: TransferRequest): TransferResponse {
    val httpResponse = httpClient.execute(transferRequest.httpRequest)
    val httpResponseNoBody = httpResponse.copy(body = null)
    val body = httpResponse.body

    if (!httpResponse.isSuccessful || body == null) {
      return TransferResponse(
        httpResponse = httpResponseNoBody,
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
      httpResponse = httpResponseNoBody,
      etag = putObjectResponse.etag,
    )
  }
}
