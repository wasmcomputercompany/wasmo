package com.wasmo.framework

import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

sealed interface ActionRegistration {
  /**
   * A pattern to match the request hostname, or null to match any hostname.
   */
  val host: Regex?

  /**
   * An HTTP method like `GET` or `POST`, or null to match any HTTP method.
   */
  val method: String?

  /**
   * A path like `/`, `/sign-out` or `/after-checkout/{checkoutSessionId}`. If a path segment is in
   * curly braces, that matches any path segment.
   */
  val path: String?

  data class Rpc<R, S>(
    override val host: Regex? = null,
    override val path: String? = null,
    val requestAdapter: KSerializer<R>,
    val responseAdapter: KSerializer<S>,
    val action: KClass<out RpcAction<R, S>>,
  ) : ActionRegistration {
    override val method: String?
      get() = null
  }

  data class Http(
    override val host: Regex? = null,
    override val path: String? = null,
    override val method: String? = null,
    val action: KClass<out HttpAction>,
  ) : ActionRegistration

  data class StaticResources(
    override val host: Regex,
    override val path: String,
    val basePackage: String,
  ) : ActionRegistration {
    override val method: String
      get() = "GET"
  }

  companion object {
    inline fun <A : RpcAction<R, S>, reified R, reified S> Rpc(
      host: Regex? = null,
      path: String? = null,
      action: KClass<out RpcAction<R, S>>,
    ) = Rpc(
      host = host,
      path = path,
      requestAdapter = serializer<R>(),
      responseAdapter = serializer<S>(),
      action = action,
    )
  }
}

interface HttpAction {
  suspend operator fun invoke(
    url: Url,
    request: Request,
  ): Response<ResponseBody>
}

interface RpcAction<R, S> {
  suspend operator fun invoke(request: R, url: Url): Response<S>
}
