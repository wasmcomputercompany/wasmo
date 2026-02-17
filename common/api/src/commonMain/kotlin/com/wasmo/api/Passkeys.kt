package com.wasmo.api

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import okio.ByteString

/** How frequently clients should refresh their server-provided challenges. */
val CHALLENGE_LIFETIME = 30.minutes

/** How much extra time we permit a challenge to be used. */
val CHALLENGE_LIFETIME_MAX_STALE = 5.minutes

@Serializable
data class AuthenticatePasskeyRequest(
  val authentication: PasskeyAuthentication,
)

@Serializable
data class AuthenticatePasskeyResponse(
  val account: AccountSnapshot,
)

/**
 * The result of successfully signing in with a passkey.
 *
 * This isn't our format! It's defined here: https://webauthn.passwordless.id/authentication/
 */
@Serializable
data class PasskeyAuthentication(
  val id: String,
  val response: Response,
) {
  @Serializable
  data class Response(
    val authenticatorData: @Serializable(Base64UrlSerializer::class) ByteString,
    val clientDataJSON: @Serializable(Base64UrlSerializer::class) ByteString,
    val signature: @Serializable(Base64UrlSerializer::class) ByteString,
  )
}

@Serializable
data class RegisterPasskeyRequest(
  val registration: PasskeyRegistration,
)

@Serializable
data class RegisterPasskeyResponse(
  val account: AccountSnapshot,
)

/**
 * The result of successfully creating a passkey.
 *
 * This isn't our format! It's defined here: https://webauthn.passwordless.id/registration/
 */
@Serializable
data class PasskeyRegistration(
  val type: String,
  val response: Response,
) {
  @Serializable
  data class Response(
    val attestationObject: @Serializable(Base64UrlSerializer::class) ByteString,
    val clientDataJSON: @Serializable(Base64UrlSerializer::class) ByteString,
    val publicKeyAlgorithm: Int,
    val transports: List<String>,
  )
}

@Serializable
data class PasskeySnapshot(
  val authenticator: String?,
  val createdAt: Instant,
)
