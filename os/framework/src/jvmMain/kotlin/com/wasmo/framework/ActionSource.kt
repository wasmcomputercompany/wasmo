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
    fun host(hostPattern: Regex, build: Binder.() -> Unit)

    fun host(host: String, build: Binder.() -> Unit)

    fun route(path: String, method: String = "GET", build: Binder.() -> Unit)

    fun routeAll(build: Binder.() -> Unit)

    fun <R, S> rpc(
      path: String,
      requestAdapter: KSerializer<R>,
      responseAdapter: KSerializer<S>,
      action: suspend (UserAgent, R, Url) -> Response<S>,
    )

    fun httpAction(
      action: suspend (UserAgent, Url, Request) -> Response<ResponseBody>,
    )

    fun staticResources(remotePath: String, basePackage: String)
  }
}

inline fun <reified R, reified S> ActionSource.Binder.rpc(
  path: String,
  noinline action: suspend (UserAgent, R, Url) -> Response<S>,
) {
  rpc(path, serializer<R>(), serializer<S>(), action)
}
