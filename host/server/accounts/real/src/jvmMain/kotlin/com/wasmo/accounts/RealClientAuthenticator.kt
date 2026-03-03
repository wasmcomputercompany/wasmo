package com.wasmo.accounts

import com.wasmo.accounts.ClientAuthenticator.UserAgent
import com.wasmo.common.tokens.newToken
import com.wasmo.deployment.Deployment
import com.wasmo.framework.UnauthorizedException
import kotlin.time.Clock

class RealClientAuthenticator private constructor(
  val clock: Clock,
  val deployment: Deployment,
  val sessionCookieSpec: SessionCookieSpec,
  val sessionCookieEncoder: SessionCookieEncoder,
  val cookieClientFactory: CookieClient.Factory,
  val userAgent: UserAgent,
) : ClientAuthenticator {
  /**
   * Send the customer's cookie on each page load. The cookie expires after 400 days, so we send
   * it on every page load to push that back.
   */
  override fun updateSessionCookie(): SessionCookie {
    val cookie = userAgent.getCookie(sessionCookieSpec.cookieName)
      ?.let { sessionCookieEncoder.decode(it) }
      ?: newSessionCookie()
    userAgent.append(cookie)
    return cookie
  }

  /** Replace the customer's cookie with a new one. */
  override fun signOutSessionCookie() {
    userAgent.append(newSessionCookie())
  }

  private fun newSessionCookie() = SessionCookie(
    token = newToken(),
    issuedAt = clock.now(),
  )

  private fun UserAgent.append(cookie: SessionCookie) {
    setCookie(
      name = sessionCookieSpec.cookieName,
      value = sessionCookieEncoder.encode(cookie),
      secure = sessionCookieSpec == SessionCookieSpec.Https,
      httpOnly = sessionCookieSpec == SessionCookieSpec.Https,
      domain = ".${deployment.baseUrl.host}",
      path = "/",
      maxAgeSeconds = 34560000L, // 400 days, the limit.
    )
  }

  override fun get(): Client {
    val sessionCookieString = userAgent.getCookie(sessionCookieSpec.cookieName)
      ?: throw UnauthorizedException("required session cookie not set")

    val sessionCookie = sessionCookieEncoder.decode(sessionCookieString)
      ?: throw UnauthorizedException("failed to decode session cookie")

    return cookieClientFactory.create(
      sessionCookie = sessionCookie,
      userAgent = userAgent.userAgent,
      ip = userAgent.ip,
    )
  }

  override fun unauthenticated(): Caller = UnauthenticatedCaller(
    userAgent = userAgent.userAgent,
    ip = userAgent.ip,
  )

  class Factory(
    val clock: Clock,
    val deployment: Deployment,
    val sessionCookieSpec: SessionCookieSpec,
    val sessionCookieEncoder: SessionCookieEncoder,
    val cookieClientFactory: CookieClient.Factory,
  ) : ClientAuthenticator.Factory {
    override fun create(userAgent: UserAgent) = RealClientAuthenticator(
      clock = clock,
      deployment = deployment,
      sessionCookieSpec = sessionCookieSpec,
      sessionCookieEncoder = sessionCookieEncoder,
      cookieClientFactory = cookieClientFactory,
      userAgent = userAgent,
    )
  }
}
