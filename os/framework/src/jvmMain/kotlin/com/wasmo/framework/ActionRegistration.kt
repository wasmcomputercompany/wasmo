package com.wasmo.framework

import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

sealed interface ActionRegistration {
  data class Rpc<R, S>(
    val pattern: HttpRequestPattern,
    val requestAdapter: KSerializer<R>,
    val responseAdapter: KSerializer<S>,
    val action: KClass<out RpcAction<R, S>>,
  ) : ActionRegistration {
    init {
      require(pattern.method == null) { "unexpected method on RPC" }
    }
  }

  data class Http(
    val pattern: HttpRequestPattern,
    val action: KClass<out HttpAction>,
  ) : ActionRegistration

  data class StaticResources(
    val host: Regex,
    val pathPrefix: String,
    val basePackage: String,
  ) : ActionRegistration

  companion object {
    inline fun <reified R, reified S> Rpc(
      pattern: HttpRequestPattern,
      action: KClass<out RpcAction<R, S>>,
    ) = Rpc(
      pattern = pattern,
      requestAdapter = serializer<R>(),
      responseAdapter = serializer<S>(),
      action = action,
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
