package com.wasmo.http

import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

object ContentType {
  const val Json = "application/json"
}

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
) {
  val isSuccessful: Boolean
    get() = code in 200..299

  val contentType: String?
    get() = headers.firstOrNull { it.name.equals(other = "Content-Type", ignoreCase = true) }?.value

  companion object {
    inline operator fun <reified T> invoke(
      json: Json,
      code: Int = 200,
      headers: List<Header> = listOf(),
      body: T,
    ) = HttpResponse(
      code = code,
      headers = headers + Header("Content-Type", ContentType.Json),
      body = json.encodeToString<T>(body).encodeUtf8(),
    )
  }
}

/** HTTP 400. */
class BadRequestException(message: String) : RuntimeException(message)

/** HTTP 404. */
class NotFoundException(message: String) : RuntimeException(message)
