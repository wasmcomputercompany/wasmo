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
  val decision: CreateUsernameDecision,
  val account: AccountSnapshot?
)

enum class CreateUsernameDecision {
  /** The new username specified in the request has been created and the caller has been logged in. */
  Success,

  /** The chosen username already exists (deleted, cannot be reused). */
  UsernameDeleted,

  /** The chosen username already exists (not deleted). */
  UsernameTaken,
}

@Serializable
data class LinkUsernameRequest(
  val username: UsernameSlug,
)

@Serializable
data class LinkUsernameResponse(
  val decision: LinkUsernameDecision,
  val account: AccountSnapshot?,
)

enum class LinkUsernameDecision {
  /** The caller's cookie was attached to the existing username specified in the request. */
  Success,

  /** The chosen username exists but has been deleted and cannot be reused. */
  UsernameDeleted,

  /** The chosen username does not exist, not even in deleted form. */
  UsernameNotFound,
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
