package com.wasmo

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

class RealHttpClient(
  private val callFactory: Call.Factory,
) : HttpClient {
  override suspend fun execute(request: HttpRequest): HttpResponse {
    val call = callFactory.newCall(request.toOkHttp())
    return suspendCancellableCoroutine { continuation ->
      continuation.invokeOnCancellation {
        call.cancel()
      }
      call.enqueue(
        object : Callback {
          override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
          }

          override fun onResponse(call: Call, response: Response) {
            response.use {
              continuation.resume(response.toWasmo())
            }
          }
        },
      )
    }
  }
}

internal fun HttpRequest.toOkHttp(): Request {
  var contentType: MediaType? = null
  val headersBuilder = Headers.Builder()

  for (header in headers) {
    when {
      header.name.equals("Content-Type", ignoreCase = true) -> {
        contentType = header.value.toMediaType()
      }

      else -> {
        headersBuilder.add(header.name, header.value)
      }
    }
  }

  return Request(
    url = url,
    headers = headersBuilder.build(),
    method = method,
    body = body?.toRequestBody(contentType),
  )
}

internal fun Response.toWasmo(): HttpResponse {
  val headers = headers
    .map { Header(it.first, it.second) }
    .toMutableList()

  val contentType = body.contentType()
  if (contentType != null) {
    headers += Header("Content-Type", contentType.toString())
  }

  return HttpResponse(
    code = code,
    headers = headers,
    body = body.byteString(),
  )
}
