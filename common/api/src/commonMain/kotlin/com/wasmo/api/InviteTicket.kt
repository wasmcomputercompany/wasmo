package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class InviteTicket(
  val code: String,
  val claimed: Boolean,
)
