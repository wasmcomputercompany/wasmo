package com.wasmo.accounts

import com.wasmo.accounts.ClientAuthenticator.UserAgent
import com.wasmo.deployment.Deployment
import com.wasmo.framework.UnauthorizedUserException
import com.wasmo.support.tokens.newToken
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Clock

@AssistedInject
class RealClientAuthenticator(
  private val clock: Clock,
  private val deployment: Deployment,
  private val sessionCookieSpec: SessionCookieSpec,
  private val sessionCookieEncoder: SessionCookieEncoder,
  private val cookieClientFactory: CookieClient.Factory,
  @Assisted private val userAgent: UserAgent,
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
      ?: throw UnauthorizedUserException("required session cookie not set")

    val sessionCookie = sessionCookieEncoder.decode(sessionCookieString)
      ?: throw UnauthorizedUserException("failed to decode session cookie")

    return cookieClientFactory.create(
      sessionCookie = sessionCookie,
      userAgent = userAgent.userAgent,
      ip = userAgent.ip,
    )
  }

  @AssistedFactory
  interface Factory : ClientAuthenticator.Factory {
    override fun create(userAgent: UserAgent): RealClientAuthenticator
  }
}
