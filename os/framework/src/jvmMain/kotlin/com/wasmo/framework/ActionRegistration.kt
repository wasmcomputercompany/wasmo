package com.wasmo.framework

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

sealed interface ActionRegistration {
  data class Rpc<R, S>(
    val pattern: HttpRequestPattern,
    val requestAdapter: KSerializer<R>,
    val responseAdapter: KSerializer<S>,
    val action: () -> RpcAction<R, S>,
  ) : ActionRegistration {
    init {
      require(pattern.method == null) { "unexpected method on RPC" }
    }
  }

  data class Http(
    val pattern: HttpRequestPattern,
    val action: () -> HttpAction,
  ) : ActionRegistration

  data class StaticResources(
    val host: Regex,
    val pathPrefix: String,
    val basePackage: String,
  ) : ActionRegistration

  companion object {
    fun Http(
      pattern: HttpRequestPattern,
      action: suspend (UserAgent, Url, Request) -> Response<ResponseBody>,
    ) = Http(
      pattern = pattern,
      action = {
        object : HttpAction {
          override suspend fun invoke(
            userAgent: UserAgent,
            url: Url,
            request: Request,
          ) = action(userAgent, url, request)
        }
      },
    )

    inline fun <reified R, reified S> Rpc(
      pattern: HttpRequestPattern,
      noinline action: suspend (UserAgent, R, Url) -> Response<S>,
    ) = Rpc<R, S>(
      pattern = pattern,
      requestAdapter = serializer<R>(),
      responseAdapter = serializer<S>(),
      action = {
        object : RpcAction<R, S> {
          override suspend fun invoke(
            userAgent: UserAgent,
            request: R,
            url: Url,
          ): Response<S> {
            return action(userAgent, request, url)
          }
        }
      },
    )
  }
}

interface HttpAction {
  suspend operator fun invoke(
    userAgent: UserAgent,
    url: Url,
    request: Request,
  ): Response<ResponseBody>
}

interface RpcAction<R, S> {
  suspend operator fun invoke(userAgent: UserAgent, request: R, url: Url): Response<S>
}
