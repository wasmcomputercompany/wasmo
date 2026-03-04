package com.wasmo.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateComputerSpecRequest(
  val computerSpecToken: String,
  val slug: ComputerSlug,
)

@Serializable
data class CreateComputerSpecResponse(
  val checkoutSessionClientSecret: String,
)
