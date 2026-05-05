package com.wasmo.identifiers

/**
 * Matches hostnames used to serve Wasmo HTTP calls.
 */
data class HostnamePatterns(
  /** A URL like `https://wasmo.com`. */
  val osHostname: Regex,

  /** Computer URLs like `https://jesse99.wasmo.com`. */
  val computerRegex: Regex,

  /** App install URLs like `https://journal-jesse99.wasmo.com`. */
  val appRegex: Regex,
)

fun Deployment.hostnamePatterns(): HostnamePatterns {
  val osHostname = baseUrl.topPrivateDomain()!!
  val suffixRegex = Regex.escape(".${osHostname}")
  return HostnamePatterns(
    osHostname = Regex(Regex.escape(osHostname)),
    computerRegex = Regex("${ComputerSlugRegex.pattern}$suffixRegex"),
    appRegex = Regex("${AppSlugRegex.pattern}-${ComputerSlugRegex.pattern}$suffixRegex"),
  )
}
