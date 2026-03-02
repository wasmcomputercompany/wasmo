package com.wasmo.api

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
data class CreateComputerRequest(
  val computerSpecToken: String,
  val slug: ComputerSlug,
)

@Serializable
data class CreateComputerResponse(
  val checkoutSessionClientSecret: String,
)

/** Between 1 and 15 letters or digits, and the first is not a digit. */
val ComputerSlugRegex = Regex("[a-z][a-z0-9]{0,14}")

@Serializable
data class ComputerSnapshot(
  val slug: ComputerSlug,
  val apps: List<InstalledApp>,
)

@Serializable
@JvmInline
value class ComputerSlug(val value: String) {
  init {
    require(value.matches(ComputerSlugRegex)) {
      "unexpected computer: $value"
    }
  }
}

