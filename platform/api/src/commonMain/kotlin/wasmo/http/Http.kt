package wasmo.http

import okio.ByteString
import wit.wasmo.http.Types.Header
import wit.wasmo.http.Types.HttpRequest as WitHttpRequest
import wit.wasmo.http.Types.HttpResponse as WitHttpResponse
import wit.wasmo.http.Types.Url

interface HttpService {
  suspend fun execute(request: WitHttpRequest): WitHttpResponse
}

fun HttpRequest(
  method: String = "GET",
  url: String,
  headers: List<Header> = listOf(),
  body: ByteString? = null,
) = WitHttpRequest(
  method,
  Url(url),
  headers,
  body,
)

fun HttpResponse(
  code: UInt = 200U,
  headers: List<Header> = listOf(),
  body: ByteString = ByteString.EMPTY,
  unused: Unit = Unit,
) = WitHttpResponse(
  code,
  headers,
  body,
)

val WitHttpRequest.contentType: String?
  get() = headers.firstOrNull { it.name.equals(other = "content-type", ignoreCase = true) }?.value

val WitHttpResponse.isSuccessful: Boolean
  get() = code in 200U..299U

val WitHttpResponse.contentType: String?
  get() = headers.firstOrNull { it.name.equals(other = "content-type", ignoreCase = true) }?.value
