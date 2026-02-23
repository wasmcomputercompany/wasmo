package com.wasmo.common.routes

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

actual fun String.decodeUrl(): Url {
  val httpUrl = toHttpUrl()
  val (topPrivateDomain, subdomain) = decodeDomain(httpUrl.host)

  val query = mutableListOf<QueryParameter>()
  for (i in 0 until httpUrl.querySize) {
    query += QueryParameter(httpUrl.queryParameterName(i), httpUrl.queryParameterValue(i))
  }

  return Url(
    scheme = httpUrl.scheme,
    topPrivateDomain = topPrivateDomain,
    subdomain = subdomain,
    path = httpUrl.pathSegments,
    query = query,
  )
}

actual fun Url.encode(): String {
  val host = when {
    subdomain != null -> "$subdomain.$topPrivateDomain"
    else -> topPrivateDomain
  }

  val builder = HttpUrl.Builder()
    .scheme(scheme)
    .host(host)

  for (pathSegment in path) {
    builder.addPathSegment(pathSegment)
  }

  if (query != null) {
    for ((name, value) in query) {
      builder.addQueryParameter(name, value)
    }
  }

  return builder.build().toString()
}
