package com.wasmo.accounts

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * The decoded contents of our session cookie.
 */
@Serializable
data class SessionCookie(
  val token: String,
  val issuedAt: Instant,
)
