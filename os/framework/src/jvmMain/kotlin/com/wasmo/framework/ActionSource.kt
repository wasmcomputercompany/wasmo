package com.wasmo.framework

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface ActionSource {
  /** For precedence; lower items are bound earlier. */
  val order: Int

  context(binder: Binder)
  fun bindActions()

  /**
   * Use this to register HTTP actions at service start up.
   */
  interface Binder {
    fun <R, S> rpc(
      pattern: HttpRequestPattern,
      requestAdapter: KSerializer<R>,
      responseAdapter: KSerializer<S>,
      action: suspend (UserAgent, R, Url) -> Response<S>,
    )

    fun httpAction(
      pattern: HttpRequestPattern,
      action: suspend (UserAgent, Url, Request) -> Response<ResponseBody>,
    )

    fun staticResources(
      host: Regex,
      pathPrefix: String,
      basePackage: String
    )
  }
}

inline fun <reified R, reified S> ActionSource.Binder.rpc(
  pattern: HttpRequestPattern,
  noinline action: suspend (UserAgent, R, Url) -> Response<S>,
) {
  rpc(pattern, serializer<R>(), serializer<S>(), action)
}
