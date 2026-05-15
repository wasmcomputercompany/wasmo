package com.wasmo.api

import com.wasmo.identifiers.UsernameSlug
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CreateUsernameRequest(
  val username: UsernameSlug,
)

@Serializable
data class CreateUsernameResponse(
  val decision: UsernameDecision,
  val account: AccountSnapshot?
)

@Serializable
data class LinkUsernameRequest(
  val username: UsernameSlug,
)

@Serializable
data class LinkUsernameResponse(
  val decision: UsernameDecision,
  val account: AccountSnapshot?,
)

enum class UsernameDecision {
  /** The caller's cookie was attached to the (new or existing) username specified in the request. */
  Success,
  /** Bad request, e.g. an invalid username was provided or a username is already linked. */
  BadRequest,
}

@Serializable
data class LinkedUsernameSnapshot(
  val linkedAt: Instant,
  val username: UsernameSlug,
)

@Serializable
data class SignInSnapshot(
  val usernameOptions: List<UsernameSlug> = listOf(),
  val canCreateUsername: Boolean = false,
)
