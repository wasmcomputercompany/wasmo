package com.wasmo.passwords

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.Argon2LibraryConfig
import com.wasmo.identifiers.Argon2WasmoConfig
import com.wasmo.identifiers.HashingPepper
import com.wasmo.identifiers.HashingSalt
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.PasswordDigest
import com.wasmo.identifiers.PasswordHash
import com.wasmo.passwords.PasswordHasher.SaltFactory.Companion.SECURE_RANDOM
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.security.MessageDigest
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.bouncycastle.crypto.PasswordConverter
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters


/**
 * Produces salted, peppered [PasswordDigest]s containing metadata about the hashing algorithm
 * configuration, the raw hash and the salt.
 *
 * JSON serialization of example value produced:
 *
 * `"{"h":"f8b405fb235ebfa0be608227e264df129e1b06856104478501ea8f57a0076713","s":"e1b2f15c648ce5eee4aca0542845fc8f","libConfig":{"m":12288,"it":3,"p":1,"sb":16,"hb":32,"v":19,"t":2},"config":{"pb":16,"v":1}}"`
 */
@Inject
@SingleIn(OsScope::class)
class Argon2PasswordHasher(
  pepper: HashingPepper,
  private val saltFactory: SaltFactory = SECURE_RANDOM,
): PasswordHasher(pepper) {

  /** These parameter values were decided in docs/code/accounts_and_authentication.md */
  private val libraryConfig: Argon2LibraryConfig = Argon2LibraryConfig(
    memoryKiBytes = 12288,
    iterations = 3,
    parallelism = 1,
    saltNumBytes = 16,
    hashNumBytes = 32,
    version = Argon2Parameters.ARGON2_VERSION_13,
    type = Argon2Parameters.ARGON2_id,
  )

  private val wasmoConfig: Argon2WasmoConfig = Argon2WasmoConfig(
    pepperNumBytes = 16,
    /** increment this if we make not backwards compatible changes in Wasmo. */
    wasmoHasherVersion = 1,
  )

  init {
    require(wasmoConfig.pepperNumBytes == pepper.value.size) {
      "Expected pepper size ${wasmoConfig.pepperNumBytes} bytes, got ${pepper.value.size}."
    }
  }

  private fun additionalBytes(
    password: AccountPassword,
    accountId: AccountId,
  ): ByteString {
    val buffer = Buffer()
    buffer.writeUtf8(password.secretValue)
    buffer.write(pepper.value)
    buffer.writeLongLe(accountId.id)
    return buffer.readByteString()
  }

  override fun digest(
    password: AccountPassword,
    accountId: AccountId,
  ): PasswordDigest {
    val salt = saltFactory.create(libraryConfig.saltNumBytes)
    require(libraryConfig.saltNumBytes == salt.value.size) {
      "Expected salt size ${libraryConfig.saltNumBytes} bytes, got ${salt.value.size}."
    }
    val passwordHash = hash(password, accountId, salt)
    return PasswordDigest(
      hash = passwordHash,
      salt = salt,
      argon2LibraryConfig = libraryConfig,
      argon2WasmoConfig = wasmoConfig,
    )
  }

  private fun hash(password: AccountPassword, accountId: AccountId, salt: HashingSalt): PasswordHash {
    val additional: ByteString = additionalBytes(password, accountId)
    val parameters = libraryConfig.toBouncyCastleParameters(salt, additional)
    val generator = Argon2BytesGenerator()
    generator.init(parameters)
    val passwordHashBytes = ByteArray(libraryConfig.hashNumBytes)
    val len = generator.generateBytes(
      /* password= */ password.secretValue.toCharArray(),
      /* out = */ passwordHashBytes,
    )
    check(len == passwordHashBytes.size) {
      "Expected ${passwordHashBytes.size} bytes, got $len"
    }
    return PasswordHash(passwordHashBytes.toByteString())
  }

  override fun verify(
    password: AccountPassword,
    accountId: AccountId,
    passwordDigest: PasswordDigest,
  ): Boolean {
    // Guard against backwards compat cases that are not currently implemented.
    // (As of 2026-08, none are implemented).
    require(libraryConfig == passwordDigest.argon2LibraryConfig) {
      "passwordDigest libraryConfig differs from ours. Cannot verify: $libraryConfig vs. ${passwordDigest.argon2LibraryConfig}."
    }
    require(wasmoConfig == passwordDigest.argon2WasmoConfig) {
      "passwordDigest's wasmoConfig differs from ours. Cannot verify: Ours=`$wasmoConfig`, theirs=`${passwordDigest.argon2LibraryConfig}`."
    }
    val salt = passwordDigest.salt
    val passwordHash = hash(password, accountId, salt)
    // Constant-time equals to prevent time based oracle attacks.
    return MessageDigest.isEqual(
      passwordHash.value.toByteArray(),
      passwordDigest.hash.value.toByteArray(),
    )
  }
}

private fun Argon2LibraryConfig.toBouncyCastleParameters(salt: HashingSalt, additional: ByteString): Argon2Parameters =
  Argon2Parameters.Builder(type)
    .withVersion(version)
    .withMemoryAsKB(memoryKiBytes)
    .withIterations(iterations)
    .withParallelism(parallelism)
    .withCharToByteConverter(PasswordConverter.UTF8)
    .withSalt(salt.value.toByteArray())
    .withAdditional(additional.toByteArray())
    .build()
