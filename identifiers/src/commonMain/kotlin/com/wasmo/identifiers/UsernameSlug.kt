package com.wasmo.identifiers

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * A valid username. All characters allowable in a username are URL safe.
 */
@Serializable
@JvmInline
value class UsernameSlug(val value: String) {

  init {
    require(isUsernameValid(value)) {
      "invalid username: $value"
    }
  }

  // DB required normalizedValue to be UNIQUE among usernames
  val normalizedValue: String
    get() = normalize(value)

  companion object {
    fun normalize(value: String): String = value.replace(Regex("[._-]"), "").lowercase()
  }
}

/**
 * 3-20 characters that are alphanumeric or limited punctuation `[._-]`. No punctuation in first or
 * last position, and not more than one in a row. No digits [0-9] in the first position.
 */
private val UsernameSlugRegex = Regex("^[a-zA-Z][a-zA-Z0-9._-]+[a-zA-Z0-9]$")

fun isUsernameValid(usernameString: String): Boolean =
  // Check the length first rather than get DoSed matching the regex against a very long String
  usernameString.length in 3..20
    && UsernameSlugRegex.matches(usernameString)
    && Regex("[._-]{2}") !in usernameString
