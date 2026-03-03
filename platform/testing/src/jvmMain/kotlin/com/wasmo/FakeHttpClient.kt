package com.wasmo

import com.wasmo.http.HttpClient
import com.wasmo.http.HttpRequest
import com.wasmo.http.HttpResponse
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(AppScope::class)
class FakeHttpClient : HttpClient {
  private val handlersFlow = MutableStateFlow(listOf<Handler>())

  operator fun plusAssign(handler: Handler) {
    handlersFlow.update { it + handler }
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    return handlersFlow.mapNotNull { handlers ->
      handlers.firstNotNullOfOrNull {
        it.handle(request)
      }
    }.first()
  }

  interface Handler {
    fun handle(request: HttpRequest): HttpResponse?
  }
}
