package com.wasmo.api

import kotlinx.serialization.Serializable
import okio.ByteString

/**
 * @param nextChallenge expires after [CHALLENGE_LIFETIME].
 */
@Serializable
data class AccountSnapshot(
  val nextChallenge: @Serializable(Base64UrlSerializer::class) ByteString,
  val passkeys: List<PasskeySnapshot>,
)
