package wasmo.http

import okio.ByteString

interface HttpService {
  suspend fun execute(request: HttpRequest): HttpResponse
}

data class HttpRequest(
  val method: String = "GET",
  val url: String,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
) {
  val contentType: String?
    get() = headers.firstOrNull { it.name.equals(other = "content-type", ignoreCase = true) }?.value
}

data class Header(
  val name: String,
  val value: String,
)

data class HttpResponse(
  val code: Int = 200,
  val headers: List<Header> = listOf(),
  val body: ByteString = ByteString.EMPTY,
) {
  val isSuccessful: Boolean
    get() = code in 200..299

  val contentType: String?
    get() = headers.firstOrNull { it.name.equals(other = "content-type", ignoreCase = true) }?.value
}
