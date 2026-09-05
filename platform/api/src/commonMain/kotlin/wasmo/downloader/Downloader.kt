package wasmo.downloader

import wit.wasmo.http.HttpRequest
import wit.wasmo.http.HttpResponse

interface Downloader {
  /**
   * Make an HTTP request and save its response to the object store.
   *
   * This will only write to the object store if the HTTP response is
   * successful.
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
