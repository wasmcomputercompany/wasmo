package com.wasmo.framework

import org.w3c.dom.url.URL

actual fun String.decodeUrl(): Url {
  val url = URL(this)

  val (topPrivateDomain, subdomain) = decodeDomain(url.hostname)
  val path = url.pathname.removePrefix("/").split("/")
  val query = mutableListOf<QueryParameter>()

  val iterator = url.searchParams.asDynamic().entries()
  while (true) {
    val entry = iterator.next()
    if (entry.done) break
    query += QueryParameter(entry.value[0], entry.value[1])
  }

  val scheme = url.protocol.removeSuffix(":")
  return Url(
    scheme = scheme,
    topPrivateDomain = topPrivateDomain,
    port = url.port.toIntOrNull() ?: defaultPort(scheme),
    subdomain = subdomain,
    path = path,
    query = query,
  )
}

actual fun Url.encode(): String {
  return toURL().href
}

actual fun Url.encodePathAndQuery(): String {
  val url = toURL()
  return "${url.pathname}${url.search}"
}

fun Url.toURL(): URL {
  val hostname = when {
    subdomain != null -> "$subdomain.$topPrivateDomain"
    else -> topPrivateDomain
  }
  val pathname = path.joinToString(separator = "/")
  val result = URL("$scheme:$hostname:${port}/$pathname")
  if (query != null) {
    for ((name, value) in query) {
      result.searchParams.append(name, value.toString())
    }
  }
  return result
}
