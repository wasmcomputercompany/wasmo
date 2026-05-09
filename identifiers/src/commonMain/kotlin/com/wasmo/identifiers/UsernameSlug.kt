package com.wasmo.identifiers

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class UsernameSlug(val value: String) {
  init {
    require(value.matches(UsernameSlugRegex)) {
      "invalid username: $value"
    }
  }

  companion object {
    fun of(unnormalizedValue: String) = UsernameSlug(unnormalizedValue.lowercase())
  }

}

/** Between 1 and 15 lowercase (for case insensitivity) letters or digits, and the first is not a digit. */
val UsernameSlugRegex = Regex("[a-z][a-z0-9]{0,14}")
