package com.wasmo.api

import com.wasmo.identifiers.UsernameSlug
import kotlinx.serialization.Serializable
import okio.ByteString

/**
 * @param nextChallenge expires after [CHALLENGE_LIFETIME].
 */
@Serializable
data class AccountSnapshot(
  val nextChallenge: @Serializable(Base64UrlSerializer::class) ByteString,
  val passkeys: List<PasskeySnapshot>,
  val emailAddresses: List<LinkedEmailAddressSnapshot>,
  val username: LinkedUsernameSnapshot?,
  val hasInvite: Boolean,
  /** Suitable for identifying the signed-in account in the UI, or null if not signed-in. */
  val signedInAccountName: String?,
) {
  val isSignedIn: Boolean
    get() = signedInAccountName != null
}

@Serializable
data object AccountSnapshotRequest

@Serializable
data class AccountSnapshotResponse(
  val account: AccountSnapshot,
)
