package com.wasmo

interface Downloader {
  /**
   * Make an HTTP request and save its response to the object store.
   *
   * This will only write to the object store if the HTTP response is
   * [successful][HttpResponse.isSuccessful].
   */
  suspend fun download(transferRequest: TransferRequest): TransferResponse
}

data class TransferRequest(
  val httpRequest: HttpRequest,
  val objectStoreKey: String,
)

/**
 * @param httpResponse the response body of this response will be null.
 * @param etag absent if the file was not saved.
 */
data class TransferResponse(
  val httpResponse: HttpResponse,
  val etag: String?,
)

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
        value = body
      )
    )

    return TransferResponse(
      httpResponse = httpResponseNoBody,
      etag = putObjectResponse.etag,
    )
  }
}
