package com.wasmo.accounts

import com.wasmo.framework.UserAgent

interface ClientAuthenticator {
  fun updateSessionCookie(): SessionCookie
  fun signOutSessionCookie()
  fun get(): Client

  interface Factory {
    fun create(userAgent: UserAgent): ClientAuthenticator
  }
}
