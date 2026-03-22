package com.wasmo.testing.framework

import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

/** Returns a [ResponseBody] that's a data class appropriate for tests. */
@Suppress("UNCHECKED_CAST") // Changing the type and type parameter.
fun Response<ResponseBody>.snapshot(): Response<ResponseBodySnapshot> {
  val bodyByteString = Buffer().run {
    body.write(this)
    readByteString()
  }
  return copy(
    body = ResponseBodySnapshot(bodyByteString),
  ) as Response<ResponseBodySnapshot>
}

data class ResponseBodySnapshot(
  val body: ByteString,
) : ResponseBody {
  constructor(body: String) : this(body.encodeUtf8())

  override fun write(sink: BufferedSink) {
    sink.write(body)
  }
}
