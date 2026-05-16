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

  /**
   * The chosen username exists but has been deleted and cannot be reused.
   */
  UsernameDeleted,
  /**
   * The client asked to link a new username but it already exists (and wasn't
   * deleted), or to link an existing username but it doesn't exist (not even in
   * deleted form).
   */
  UsernameExistenceIncompatibleWithRequest,
  /** The client sent a bad request indicative of a programming bug. */
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
