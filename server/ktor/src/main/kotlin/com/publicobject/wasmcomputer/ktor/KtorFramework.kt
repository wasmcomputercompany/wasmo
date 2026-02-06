package com.publicobject.wasmcomputer.ktor

import com.publicobject.wasmcomputer.api.WasmComputerJson
import com.publicobject.wasmcomputer.framework.BadRequestException
import com.publicobject.wasmcomputer.framework.ContentType
import com.publicobject.wasmcomputer.framework.Response
import com.publicobject.wasmcomputer.framework.ResponseBody
import io.ktor.http.ContentType as KtorContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingRequest
import io.ktor.utils.io.asSink
import io.ktor.utils.io.asSource
import io.ktor.utils.io.charsets.Charset
import kotlinx.io.okio.asOkioSink
import kotlinx.io.okio.asOkioSource
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import okio.buffer

fun <T> DeserializationStrategy<T>.decode(request: RoutingRequest): T {
  try {
    request.receiveChannel().asSource().asOkioSource().buffer().use { source ->
      return WasmComputerJson.decodeFromString(this@decode, source.readUtf8())
    }
  } catch (e: SerializationException) {
    throw BadRequestException(e.message ?: "failed to decode JSON")
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
        sink.writeUtf8(WasmComputerJson.encodeToString(serializer, response.body))
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

private fun ContentType.toKtor(): KtorContentType {
  var result = KtorContentType(
    contentType = type,
    contentSubtype = subtype,
  )
  if (this.charset != null) {
    result = result.withCharset(Charset.forName(this.charset))
  }
  return result
}
