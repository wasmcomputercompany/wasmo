package com.wasmo.accounts

interface ClientAuthenticator {
  fun updateSessionCookie(): SessionCookie
  fun signOutSessionCookie()
  fun get(): Client
  fun unauthenticated(): Client

  interface Factory {
    fun create(userAgent: UserAgent): ClientAuthenticator
  }

  /** Minimal HTTP request/response API for getting and setting session cookies. */
  interface UserAgent {
    val userAgent: String?

    val ip: String

    /** Returns the cookie from the request, or set on the response if that exists. */
    fun getCookie(name: String): String?

    fun setCookie(
      name: String,
      value: String,
      secure: Boolean,
      httpOnly: Boolean,
      path: String,
      maxAgeSeconds: Long,
    )
  }
}
