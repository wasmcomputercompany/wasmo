package com.wasmo.api.routes

data class Url(
  val scheme: String,
  val topPrivateDomain: String,
  val port: Int = defaultPort(scheme),
  val subdomain: String? = null,
  val path: List<String> = listOf(""),
  val query: List<QueryParameter>? = listOf(),
) {
  init {
    require(scheme == "http" || scheme == "https")
    require(path.isNotEmpty())
  }
}

fun defaultPort(scheme: String): Int = when (scheme) {
  "http" -> 80
  "https" -> 443
  else -> error("unexpected scheme: $scheme")
}

data class QueryParameter(
  val name: String,
  val value: String?,
)

expect fun String.decodeUrl(): Url

expect fun Url.encode(): String

/** Return the parts of this URL that can be navigated to without a hard navigation. */
expect fun Url.encodePathAndQuery(): String

val TopPrivateDomains = listOf(
  "wasmo.com",
  "wasmo.dev",
  "wasmo.localhost",
)

internal fun decodeDomain(hostname: String): Pair<String, String?> {
  for (candidate in TopPrivateDomains) {
    if (!hostname.endsWith(candidate)) continue

    val candidateStart = hostname.length - candidate.length
    if (candidateStart == 0) {
      return candidate to null
    }

    if (candidateStart > 1 || hostname[candidateStart - 1] == '.') {
      return candidate to hostname.take(candidateStart - 1)
    }
  }

  return hostname to null
}
