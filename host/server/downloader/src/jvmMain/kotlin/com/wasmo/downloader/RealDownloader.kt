package com.wasmo.downloader

import com.wasmo.http.HttpClient
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.PutObjectRequest

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
