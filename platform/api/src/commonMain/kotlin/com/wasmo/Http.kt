package com.wasmo

import okhttp3.HttpUrl
import okio.ByteString

interface HttpClient {
  suspend fun execute(request: HttpRequest): HttpResponse
}

data class HttpRequest(
  val method: String,
  val url: HttpUrl,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
)

data class Header(
  val name: String,
  val value: String,
)

data class HttpResponse(
  val code: Int = 200,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
)

val HttpResponse.isSuccessful: Boolean
  get() = code in 200..299

val HttpResponse.contentType: String?
  get() = headers.firstOrNull { it.name.equals(other = "Content-Type", ignoreCase = true) }?.value

object ContentType {
  const val Json = "application/json"
}

/** HTTP 400. */
class BadRequestException(message: String) : RuntimeException(message)

/** HTTP 404. */
class NotFoundException(message: String) : RuntimeException(message)
