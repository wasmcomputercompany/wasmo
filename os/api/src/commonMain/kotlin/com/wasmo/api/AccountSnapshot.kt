package com.wasmo.api

import kotlinx.serialization.Serializable
import okio.ByteString

/**
 * @param nextChallenge expires after [CHALLENGE_LIFETIME].
 */
@Serializable
data class AccountSnapshot(
  val nextChallenge: @Serializable(Base64UrlSerializer::class) ByteString,
  private val passkeys: List<PasskeySnapshot>,
  private val emailAddresses: List<LinkedEmailAddressSnapshot>,
  val hasInvite: Boolean,
) {
  // TODO: make signedInSnapshot a ctor argument (changes wire format), and support
  // Username-based SignedInSnapshot server-side.
  val signedInSnapshot: SignedInSnapshot? =
    // TODO: We probably want "passkeys.isEmpty() &&", but that'll be a behavior change to before
    if (emailAddresses.isEmpty()) {
      null
    } else {
      EmailPasskeySignedInSnapshot(passkeys, emailAddresses)
    }
  val isSignedIn: Boolean
    get() = signedInSnapshot != null
}

@Serializable
data object AccountSnapshotRequest

@Serializable
data class AccountSnapshotResponse(
  val account: AccountSnapshot,
)
