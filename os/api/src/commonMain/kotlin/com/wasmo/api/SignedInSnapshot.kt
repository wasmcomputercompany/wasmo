package com.wasmo.api

import com.wasmo.identifiers.UsernameSlug
import kotlinx.serialization.Serializable

@Serializable
sealed class SignedInSnapshot

@Serializable
class EmailPasskeySignedInSnapshot(
  val passkeys: List<PasskeySnapshot>,
  val emailAddresses: List<LinkedEmailAddressSnapshot>,
) : SignedInSnapshot() {
  init {
    require(passkeys.isNotEmpty() || emailAddresses.isNotEmpty()) {
      "At least one passkey or email address required to be signed in."
    }
  }
}

@Serializable
class UsernameSignedInSnapshot(
  val username: UsernameSlug,
) : SignedInSnapshot()
