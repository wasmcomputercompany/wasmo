package com.publicobject.wasmcomputer.framework

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import okio.BufferedSink

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
)

object ContentTypes {
  val ImagePng = ContentType("image", "png")
  val TextHtml = ContentType("text", "html", "utf-8")
  val TextPlain = ContentType("text", "plain", "utf-8")
}

open class HttpException(
  val code: Int,
  message: String,
) : Exception(message) {
  fun asResponse(): Response<ResponseBody> = Response(
    status = 400,
    contentType = ContentTypes.TextPlain,
    body = ResponseBody { sink -> sink.writeUtf8(message ?: "") },
  )
}

class BadRequestException(message: String) : HttpException(400, message)

class UnauthorizedException(message: String) : HttpException(403, message)

class NotFoundException(message: String) : HttpException(404, message)

@OptIn(ExperimentalContracts::class)
fun checkRequest(value: Boolean, lazyMessage: () -> String) {
  contract {
    returns() implies value
  }
  if (!value) {
    throw BadRequestException(lazyMessage())
  }
}
