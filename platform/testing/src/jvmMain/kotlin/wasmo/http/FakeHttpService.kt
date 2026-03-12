package wasmo.http

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import okio.ByteString.Companion.encodeUtf8

@Inject
@SingleIn(AppScope::class)
class FakeHttpService : HttpService {
  private val handlersFlow = MutableStateFlow(listOf<Handler>())

  operator fun plusAssign(handler: Handler) {
    handlersFlow.update { it + handler }
  }

  override suspend fun execute(request: HttpRequest): HttpResponse {
    return handlersFlow.value
      .firstNotNullOfOrNull { it.handle(request) }
      ?: HttpResponse(
        code = 404,
        body = "no handler for $request".encodeUtf8(),
      )
  }

  fun interface Handler {
    fun handle(request: HttpRequest): HttpResponse?
  }
}
