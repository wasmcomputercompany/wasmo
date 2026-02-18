package com.wasmo.accounts

enum class SessionCookieSpec(
  val cookieName: String,
) {
  Http(cookieName = "session"),
  Https(cookieName = "__Host-session"),
}
