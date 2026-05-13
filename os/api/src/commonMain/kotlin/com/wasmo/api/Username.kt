package com.wasmo.api

import com.wasmo.identifiers.UsernameSlug
import kotlinx.serialization.Serializable

@Serializable
data class CreateUsernameRequest(
  val username: UsernameSlug,
)

@Serializable
data object CreateUsernameResponse

@Serializable
data class LinkUsernameRequest(
  val username: UsernameSlug,
)

@Serializable
data object LinkUsernameResponse
