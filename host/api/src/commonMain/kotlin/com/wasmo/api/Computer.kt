package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateComputerRequest(
  val computerSpecToken: String,
  val slug: String,
)

@Serializable
data class CreateComputerResponse(
  val checkoutSessionClientSecret: String,
)

val ComputerSlugRegex = Regex("[a-z][a-z0-9]{0,14}")
val AppSlugRegex = Regex("[a-z][a-z0-9]{0,14}")
