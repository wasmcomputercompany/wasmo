package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class InviteTicket(
  val claimed: Boolean,
)
