package com.wasmo.identifiers

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AppSlug(val value: String) {
  init {
    require(value.matches(AppSlugRegex)) {
      "unexpected app: $value"
    }
  }
}

/** Between 1 and 15 letters or digits, and the first is not a digit. */
val AppSlugRegex = Regex("[a-z][a-z0-9]{0,14}")
