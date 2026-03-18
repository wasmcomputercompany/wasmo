package com.wasmo.ktor

import com.wasmo.api.WasmoJson
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import io.ktor.http.ContentType as KtorContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingRequest
import io.ktor.utils.io.asSink
import io.ktor.utils.io.asSource
import kotlinx.io.okio.asOkioSink
import kotlinx.io.okio.asOkioSource
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import okhttp3.MediaType
import okio.buffer

fun <T> DeserializationStrategy<T>.decode(request: RoutingRequest): T {
  try {
    request.receiveChannel().asSource().asOkioSource().buffer().use { source ->
      return WasmoJson.decodeFromString(this@decode, source.readUtf8())
    }
  } catch (e: SerializationException) {
    throw ArgumentUserException("failed to decode manifest\n\n${e.message}")
  }
}

suspend fun <T> RoutingCall.respond(
  serializer: SerializationStrategy<T>,
  response: Response<T>,
) {
  respond(
    Response(
      status = response.status,
      headers = response.headers,
      contentType = response.contentType,
      body = ResponseBody { sink ->
        sink.writeUtf8(WasmoJson.encodeToString(serializer, response.body))
      },
    ),
  )
}

suspend fun RoutingCall.respond(response: Response<ResponseBody>) {
  for (header in response.headers) {
    this.response.header(header.name, header.value)
  }
  respondBytesWriter(
    status = HttpStatusCode.fromValue(response.status),
    contentType = response.contentType?.toKtor(),
    producer = {
      asSink().asOkioSink().buffer().use { sink ->
        response.body.write(sink)
      }
    },
  )
}

private fun MediaType.toKtor(): KtorContentType {
  var result = KtorContentType(
    contentType = type,
    contentSubtype = subtype,
  )
  val charset = charset(null)
  if (charset != null) {
    result = result.withCharset(charset)
  }
  return result
}
