package com.wasmo.framework

import java.net.HttpURLConnection
import okhttp3.HttpUrl
import okio.BufferedSink
import okio.ByteString

data class Request(
  val method: String,
  val url: HttpUrl,
  val headers: List<Header> = listOf(),
  val body: ByteString? = null,
)

data class Response<T>(
  val status: Int = 200,
  val headers: List<Header> = listOf(),
  val contentType: ContentType? = null,
  val body: T,
) {
  fun header(name: String): String? {
    return headers.firstOrNull { it.name == name }?.value
  }
}

fun interface ResponseBody {
  fun write(sink: BufferedSink)
}

data class Header(
  val name: String,
  val value: String,
)

data class ContentType(
  val type: String,
  val subtype: String,
  val charset: String? = null,
) {
  override fun toString(): String = when {
    charset != null -> "$type/$subtype; charset=$charset"
    else -> "$type/$subtype"
  }
}

object ContentTypes {
  val ImagePng = ContentType("image", "png")
  val TextHtml = ContentType("text", "html", "utf-8")
  val TextPlain = ContentType("text", "plain", "utf-8")
  val ApplicationToml = ContentType("application", "toml")
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
