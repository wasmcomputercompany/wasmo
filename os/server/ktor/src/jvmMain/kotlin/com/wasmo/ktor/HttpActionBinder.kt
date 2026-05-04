package com.wasmo.ktor

import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Use this to register HTTP actions at service start up.
 */
interface HttpActionBinder {
  fun host(hostPattern: Regex, build: HttpActionBinder.() -> Unit)

  fun host(host: String, build: HttpActionBinder.() -> Unit)

  fun route(path: String, method: String = "GET", build: HttpActionBinder.() -> Unit)

  fun routeAll(build: HttpActionBinder.() -> Unit)

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

inline fun <reified R, reified S> HttpActionBinder.rpc(
  path: String,
  noinline action: suspend (UserAgent, R, Url) -> Response<S>,
) {
  rpc(path, serializer<R>(), serializer<S>(), action)
}
