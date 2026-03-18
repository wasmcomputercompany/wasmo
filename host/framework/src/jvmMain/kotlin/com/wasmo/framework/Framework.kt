package com.wasmo.framework

import java.net.HttpURLConnection
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import okio.ByteString
import wasmo.http.Header

data class Request(
  val method: String,
  val url: HttpUrl,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
)

data class Response<T>(
  val status: Int = 200,
  val headers: List<Header> = listOf(),
  val contentType: MediaType? = null,
  val body: T,
) {
  fun header(name: String): String? {
    return headers.firstOrNull { it.name == name }?.value
  }
}

fun interface ResponseBody {
  fun write(sink: BufferedSink)
}

object ContentTypes {
  val ImagePng = "image/png".toMediaType()
  val ImageSvg = "image/svg+xml".toMediaType()
  val TextHtml = "text/html; charset=utf-8".toMediaType()
  val TextPlain = "text/plain; charset=utf-8".toMediaType()
  val ApplicationToml = "application/toml".toMediaType()
}

fun UserException.asResponse(): Response<ResponseBody> {
  val status = when (this) {
    is ArgumentUserException -> 400
    is NotFoundUserException -> 404
    is UnauthorizedUserException -> 401
    is StateUserException -> 412
  }
  return Response(
    status = status,
    contentType = ContentTypes.TextPlain,
    body = ResponseBody { sink -> sink.writeUtf8(message ?: "") },
  )
}

fun redirect(url: HttpUrl): Response<ResponseBody> = Response(
  status = HttpURLConnection.HTTP_MOVED_TEMP,
  headers = listOf(
    Header("Location", url.toString()),
  ),
  body = ResponseBody { },
)
