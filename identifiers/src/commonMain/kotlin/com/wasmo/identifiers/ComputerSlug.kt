package com.wasmo.identifiers

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ComputerSlug(val value: String) {
  init {
    require(value.matches(ComputerSlugRegex)) {
      "unexpected computer: $value"
    }
  }
}

/** Between 1 and 15 letters or digits, and the first is not a digit. */
val ComputerSlugRegex = Regex("[a-z][a-z0-9]{0,14}")
