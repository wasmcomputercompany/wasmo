package com.wasmo.framework

data class HttpRequestPattern(
  /**
   * A pattern to match the request hostname, or null to match any hostname.
   */
  val host: Regex? = null,

  /**
   * An HTTP method like `GET` or `POST`, or null to match any HTTP method.
   */
  val method: String? = null,

  /**
   * A path like `/`, `/sign-out` or `/after-checkout/{checkoutSessionId}`. If a path segment is in
   * curly braces, that matches any path segment.
   */
  val path: String? = null,
) {
  constructor(
    host: String,
    path: String? = null,
    method: String? = null,
  ) : this(
    host = Regex(Regex.escape(host)),
    method = method,
    path = path,
  )

  companion object {
    val AllRequests = HttpRequestPattern()
  }
}
