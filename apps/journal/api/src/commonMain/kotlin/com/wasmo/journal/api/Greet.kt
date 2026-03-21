package com.wasmo.journal.api

import kotlinx.serialization.Serializable

@Serializable
data class GreetRequest(
  val name: String,
)

@Serializable
data class GreetResponse(
  val recentNames: List<String>,
)
