package com.wasmo.common.routes

data class Url(
  val topPrivateDomain: String,
  val subdomain: String? = null,
  val path: List<String> = listOf(),
  val query: List<QueryParameter>? = listOf(),
)

data class QueryParameter(
  val name: String,
  val value: String,
)
