package com.wasmo.api.routes

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

actual fun String.decodeUrl(): Url =
  toHttpUrl().toWasmoUrl()

fun HttpUrl.toWasmoUrl(): Url {
  val (topPrivateDomain, subdomain) = decodeDomain(host)

  val query = mutableListOf<QueryParameter>()
  for (i in 0 until querySize) {
    query += QueryParameter(queryParameterName(i), queryParameterValue(i))
  }

  return Url(
    scheme = scheme,
    topPrivateDomain = topPrivateDomain,
    port = port,
    subdomain = subdomain,
    path = pathSegments,
    query = query,
  )
}

actual fun Url.encode(): String {
  return toHttpUrl().toString()
}

actual fun Url.encodePathAndQuery(): String {
  val httpUrl = toHttpUrl()
  val encodedPath = httpUrl.encodedPath
  val encodedQuery = httpUrl.encodedQuery
  return when {
    encodedQuery != null -> "$encodedPath?$encodedQuery"
    else -> encodedPath
  }
}

fun Url.toHttpUrl(): HttpUrl {
  val host = when {
    subdomain != null -> "$subdomain.$topPrivateDomain"
    else -> topPrivateDomain
  }

  val builder = HttpUrl.Builder()
    .scheme(scheme)
    .host(host)
    .port(port)

  for (pathSegment in path) {
    builder.addPathSegment(pathSegment)
  }

  if (query != null) {
    for ((name, value) in query) {
      builder.addQueryParameter(name, value)
    }
  }

  return builder.build()
}
