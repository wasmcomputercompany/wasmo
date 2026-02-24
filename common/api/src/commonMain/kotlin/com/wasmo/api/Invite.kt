package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class InviteTicket(
  val code: String,
  val claimed: Boolean,
)

@Serializable
data object CreateInviteRequest

@Serializable
data class CreateInviteResponse(
  val inviteUrl: String,
)
