package wasmo.http

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.HttpUrl
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import wit.wasmo.http.Types.HttpRequest
import wit.wasmo.http.Types.HttpResponse

class FakeHttpService : HttpService {
  private val handlersFlow = MutableStateFlow(listOf<Handler>())
  private val responses = mutableMapOf<HttpUrl, HttpResponse>()

  operator fun plusAssign(handler: Handler) {
    handlersFlow.update { it + handler }
  }

  operator fun set(url: HttpUrl, response: HttpResponse) {
    responses[url] = response
  }

  operator fun set(url: HttpUrl, responseBody: ByteString) {
    responses[url] = HttpResponse(body = responseBody)
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    return handlersFlow.value
      .firstNotNullOfOrNull { it.handle(request) }
      ?: responses[request.httpUrl]
      ?: HttpResponse(
        code = 404U,
        body = "no handler for $request".encodeUtf8(),
      )
  }

  fun interface Handler {
    fun handle(request: HttpRequest): HttpResponse?
  }
}
