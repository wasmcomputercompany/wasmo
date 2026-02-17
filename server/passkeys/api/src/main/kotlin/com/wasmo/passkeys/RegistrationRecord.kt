package com.wasmo.passkeys

import com.wasmo.api.Base64UrlSerializer
import kotlinx.serialization.Serializable
import okio.ByteString

/**
 * Stored after a successful registration in order to later perform authentication.
 *
 * @param attestationObject a CBOR-encoded attestation. See
 *   https://www.w3.org/TR/webauthn-1/#sctn-attestation
 * @param clientDataJSON this is a ByteString and not a string because it is hashed and its exact
 *   encoding must be retained.
 */
@Serializable
data class RegistrationRecord(
  val attestationObject: @Serializable(Base64UrlSerializer::class) ByteString,
  val clientDataJSON: @Serializable(Base64UrlSerializer::class) ByteString,
  val transports: List<String>,
)

data class RegisterResult(
  val id: String,
  val aaguid: String,
  val record: RegistrationRecord,
)
