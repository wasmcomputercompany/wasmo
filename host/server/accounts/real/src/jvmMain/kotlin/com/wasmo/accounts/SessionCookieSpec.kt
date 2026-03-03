package com.wasmo.accounts

/**
 * Wasmo's session cookie should not be visible to client-side JavaScript.
 *
 * It should operate across all hostnames: `wasmo.com`, `jesse99.wasmo.com`, etc.
 */
enum class SessionCookieSpec(
  val cookieName: String,
) {
  Http(cookieName = "wasmo_session"),
  Https(cookieName = "__Http-wasmo_session"),
}
