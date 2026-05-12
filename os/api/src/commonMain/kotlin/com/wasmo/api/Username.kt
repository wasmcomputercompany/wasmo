package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateUsernameRequest(
  val username: String,
)

@Serializable
data object CreateUsernameResponse

@Serializable
data class LinkUsernameRequest(
  val username: String,
)

@Serializable
data object LinkUsernameResponse
