package com.wasmo.passwords

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.HashingPepper
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.PasswordDigest
import de.mkammerer.argon2.Argon2Factory
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.Buffer


/**
 * Produces salted, peppered [PasswordDigest]s containing metadata about the hashing algorithm
 * configuration, the raw hash and the salt.
 *
 * Example value produced:
 *
 * `"$argon2id$v=19$m=12288,t=3,p=1$jgHWZdnPF/Qg63L4FxeA7Q$MenB3YE56XIKXdICSAnzQznH+CAbe/8jF6u4Vh79Fio"`
 */
@Inject
@SingleIn(OsScope::class)
class Argon2PasswordHasher(pepper: HashingPepper): PasswordHasher(pepper) {

  /** These parameters were decided in docs/code/accounts_and_authentication.md */
  object Argon2Config {
    const val MEMORY = 12288
    const val ITERATIONS = 3
    const val PARALLELISM = 1

    val TYPE = Argon2Factory.Argon2Types.ARGON2id
  }

  // Use default salt and hash length (16 and 32 bytes, respectively, as of Argon2 library v2.12).
  private val argon2 = Argon2Factory.create(Argon2Config.TYPE)

  private fun dataBytes(
    password: AccountPassword,
    accountId: AccountId,
  ): ByteArray {
    val buffer = Buffer()
    buffer.writeUtf8(password.secretValue)
    buffer.write(pepper.value)
    buffer.writeLongLe(accountId.id)
    return buffer.readByteArray()
  }

  override fun digest(
    password: AccountPassword,
    accountId: AccountId,
  ): PasswordDigest {
    val data: ByteArray = dataBytes(password, accountId)
    // The .java wrapper of the argon2 library internally generates a new salt each time this
    // function is called; the value of that salt cannot be customized via the API surface.
    // That salt value is embedded within the returned hashString.
    val hashString = argon2.hash(Argon2Config.ITERATIONS, Argon2Config.MEMORY, Argon2Config.PARALLELISM, data)
    return PasswordDigest(hashString)
  }

  override fun verify(
    password: AccountPassword,
    accountId: AccountId,
    passwordDigest: PasswordDigest
  ): Boolean {
    val data: ByteArray = dataBytes(password, accountId)
    return argon2.verify(passwordDigest.value, data)
  }
}
