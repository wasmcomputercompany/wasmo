package com.wasmo.identifiers

import com.wasmo.json.ByteStringAsHexSerializer
import java.security.MessageDigest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

@Serializable
@JvmInline
value class AccountId(val id: Long)

// avoid putting com.wasmo.identifiers.Secure into our API signature
class AccountPassword(val secretValue: String) {
  // For private convenience only, constructs a mutable ByteArray.
  private fun secretValueBytes() = secretValue.encodeUtf8().toByteArray()

  fun isEmpty() = secretValue.isEmpty()

  fun isNotEmpty() = secretValue.isNotEmpty()

  override fun equals(other: Any?): Boolean {
    return (other is AccountPassword)
      // Constant-time (apart from secretValue length) equals
      && MessageDigest.isEqual(secretValueBytes(), other.secretValueBytes())
  }

  override fun hashCode(): Int {
    return secretValue.hashCode()
  }

  override fun toString(): String = "<redacted password>"

  companion object {
    val EMPTY = AccountPassword("")
  }
}

@JvmInline
value class HashingPepper(val value: ByteString)

@JvmInline
@Serializable
value class HashingSalt(
  @Serializable(with = ByteStringAsHexSerializer::class)
  val value: ByteString,
)

/**
 * A composite value composing salted hash of the password, the salt, and metadata (e.g. hash
 * algorithm / version used). Because the hashes are salted and peppered, digests of the same
 * password will in general not be equal.
 *
 * Argon2PasswordHasher produces passwordHashes that are salted and peppered
 */
@Serializable
data class PasswordDigest(
  @SerialName("h")
  val hash: PasswordHash,
  @SerialName("s")
  val salt: HashingSalt,
  @SerialName("libConfig")
  val argon2LibraryConfig: Argon2LibraryConfig,
  @SerialName("config")
  val argon2WasmoConfig: Argon2WasmoConfig,
)

@Serializable
@JvmInline
value class PasswordHash(
  @Serializable(with = ByteStringAsHexSerializer::class)
  @SerialName("v")
  val value: ByteString,
)

@Serializable
data class Argon2LibraryConfig(
  @SerialName("m")
  val memoryKiBytes: Int,
  @SerialName("it")
  val iterations: Int,
  @SerialName("p")
  val parallelism: Int,
  @SerialName("sb")
  val saltNumBytes: Int,
  @SerialName("hb")
  val hashNumBytes: Int,
  @SerialName("v")
  val version: Int,
  @SerialName("t")
  val type: Int,
)

@Serializable
data class Argon2WasmoConfig(
  @SerialName("pb")
  val pepperNumBytes: Int,
  @SerialName("v")
  val wasmoHasherVersion: Int,
)

@Serializable
@JvmInline
value class ComputerAccessId(val id: Long)

@Serializable
@JvmInline
value class SubscriptionPeriodId(val id: Long)

@Serializable
@JvmInline
value class ComputerId(val id: Long)

@Serializable
@JvmInline
value class ComputerSpecId(val id: Long)

@Serializable
@JvmInline
value class CookieId(val id: Long)

@Serializable
@JvmInline
value class InstalledAppId(val id: Long)

@Serializable
@JvmInline
value class InstalledAppDatabaseId(val id: Long)

@Serializable
@JvmInline
value class InstalledAppReleaseId(val id: Long)

@Serializable
@JvmInline
value class InviteId(val id: Long)

@Serializable
@JvmInline
value class LinkedEmailAddressId(val id: Long)

@Serializable
@JvmInline
value class PasskeyId(val id: Long)

@Serializable
@JvmInline
value class PermitId(val id: Long)

@Serializable
@JvmInline
value class SchemaVersionId(val id: Long)

@Serializable
@JvmInline
value class StripeCustomerId(val id: Long)

@Serializable
@JvmInline
value class PasswordDigestId(val id: Long)

@Serializable
@JvmInline
value class UserId(val id: Long)

@Serializable
@JvmInline
value class UsernameId(val id: Long)
