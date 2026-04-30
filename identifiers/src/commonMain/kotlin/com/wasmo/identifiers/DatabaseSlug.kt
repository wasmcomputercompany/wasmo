package com.wasmo.identifiers

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class DatabaseSlug(val value: String) {
  init {
    require(value.isEmpty() || DatabaseSlugRegex.matches(value)) {
      "unexpected database slug: $value"
    }
  }

  fun isEmpty() = value.isEmpty()

  override fun toString() = value
}

/** Between 1 and 15 letters or digits, and the first is not a digit. */
val DatabaseSlugRegex = Regex("[a-z][a-z0-9]{0,14}")
