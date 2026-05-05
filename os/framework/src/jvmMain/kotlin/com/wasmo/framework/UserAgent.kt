package com.wasmo.framework

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
    domain: String,
    path: String,
    maxAgeSeconds: Long,
  )
}
