package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
import io.ktor.http.CookieEncoding
import io.ktor.server.plugins.origin
import io.ktor.server.request.header
import io.ktor.server.routing.RoutingContext

class KtorUserAgent(
  private val routingContext: RoutingContext,
) : ClientAuthenticator.UserAgent {
  override val userAgent: String?
    get() = routingContext.call.request.header("user-agent")

  override val ip: String
    get() = routingContext.call.request.header("fly-client-ip")
      ?: routingContext.call.request.origin.remoteAddress

  override fun getCookie(name: String): String? {
    return routingContext.call.request.cookies[name]
      ?: routingContext.call.response.cookies[name]?.value
  }

  override fun setCookie(
    name: String,
    value: String,
    secure: Boolean,
    httpOnly: Boolean,
    path: String,
    maxAgeSeconds: Long,
  ) {
    require("\"" !in value)
    routingContext.call.response.cookies.append(
      name = name,
      value = value,
      encoding = CookieEncoding.DQUOTES,
      secure = secure,
      httpOnly = httpOnly,
      path = path,
      maxAge = maxAgeSeconds,
    )
  }
}
