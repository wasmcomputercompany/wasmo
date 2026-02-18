package com.wasmo.testing

import com.wasmo.accounts.ClientAuthenticator

class FakeUserAgent : ClientAuthenticator.UserAgent {
  override val userAgent: String
    get() = "Netscape Navigator"
  override val ip: String
    get() = "127.0.0.1"

  private val cookies = mutableMapOf<String, String>()

  override fun getCookie(name: String) = cookies[name]

  override fun setCookie(
    name: String,
    value: String,
    secure: Boolean,
    httpOnly: Boolean,
    path: String,
    maxAgeSeconds: Long,
  ) {
    cookies[name] = value
  }
}
