package com.wasmo.passwords

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.HashingPepper
import com.wasmo.identifiers.HashingSalt
import com.wasmo.identifiers.PasswordDigest
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.encodeUtf8
import org.junit.Test

class Argon2PasswordHasherTest {
  val pepper = HashingPepper("pepper for tests".encodeUtf8()) // 16 bytes
  val subject = Argon2PasswordHasher(pepper)
  val password = AccountPassword("dummy password")
  val accountId = AccountId(42L)

  @Test
  fun `passwordDigest verifies`() {
    val passwordDigest = subject.digest(password, accountId)
    assertThat(subject.verify(password, accountId,  passwordDigest)).isTrue()
  }

  @Test
  fun `successive digests use different salts causing the hashes to differ`() {
    val a = subject.digest(password, accountId)
    val b = subject.digest(password, accountId)

    assertThat(a.salt).isNotEqualTo(b.salt)
    assertThat(a.hash).isNotEqualTo(b.hash)
  }

  @Test
  fun `hash is deterministic for fixed salt and pepper`() {
    val fixedSalt = HashingSalt("test crypto salt".encodeUtf8()) // 16 bytes
    val fixedPepper = HashingPepper("pepper for tests".encodeUtf8()) // 16 bytes
    val insecureHasher = Argon2PasswordHasher(fixedPepper, { fixedSalt } )

    val a = insecureHasher.digest(password, accountId)
    val b = insecureHasher.digest(password, accountId)

    assertThat(a).isEqualTo(b)
    assertThat(a.salt).isEqualTo(fixedSalt)
  }

  @Test
  fun `backwards compat - serialization of digest is deterministic`() {
    val fixedSalt = HashingSalt("test crypto salt".encodeUtf8()) // 16 bytes
    val fixedPepper = HashingPepper("pepper for tests".encodeUtf8()) // 16 bytes
    val insecureHasher = Argon2PasswordHasher(fixedPepper, { fixedSalt } )

    val digest = insecureHasher.digest(password, accountId)
    // It's okay for this format to change, but then we need to ensure that we can still
    // parse and act on the old format correctly (backwards-compatibly).
    assertThat(Json.encodeToString(digest)).isEqualTo(
      "{\"h\":\"2b62a8e7b09da701d537c457d83d1dc9ef2672a5249546af0df5b8d21e0ec551\",\"s\":\"746573742063727970746f2073616c74\",\"libConfig\":{\"m\":12288,\"it\":3,\"p\":1,\"sb\":16,\"hb\":32,\"v\":19,\"t\":2},\"config\":{\"pb\":16,\"v\":1}}"
    )
  }

  @Test
  fun `backwards compat - password can be verified against legacy digest`() {
    // Generated via:
    // val s = Json.encodeToString(subject.digest(password, accountId))
    // As of 2026-08, this example value is quoted at the top of Argon2PasswordHasher.kt
    val legacyDigestString = "{\"h\":\"f8b405fb235ebfa0be608227e264df129e1b06856104478501ea8f57a0076713\",\"s\":\"e1b2f15c648ce5eee4aca0542845fc8f\",\"libConfig\":{\"m\":12288,\"it\":3,\"p\":1,\"sb\":16,\"hb\":32,\"v\":19,\"t\":2},\"config\":{\"pb\":16,\"v\":1}}"
    val passwordDigest = Json.decodeFromString<PasswordDigest>(legacyDigestString)
    assertThat(subject.verify(password, accountId, passwordDigest)).isTrue()

  }

  @Test
  fun `passwordDigest verifies on new subject`() {
    val passwordDigest = subject.digest(password, accountId)
    val newSubject = Argon2PasswordHasher(pepper)
    assertThat(newSubject.verify(password, accountId,  passwordDigest)).isTrue()
  }

  @Test
  fun `passwordDigest does not verify for different password`() {
    val passwordDigest = subject.digest(AccountPassword("different password"), accountId)
    assertThat(subject.verify(password, accountId, passwordDigest)).isFalse()
  }

  @Test
  fun `passwordDigest does not verify for different accountId`() {
    val passwordDigest = subject.digest(password, AccountId(accountId.id + 1))
    assertThat(subject.verify(password, accountId, passwordDigest)).isFalse()
  }

  @Test
  fun `passwordDigest does not verify for different pepper on new subject`() {
    val passwordDigest = subject.digest(password, accountId)
    val differentPapper = HashingPepper("different pepper".encodeUtf8()) // 16 bytes
    val newSubject = Argon2PasswordHasher(differentPapper)
    assertThat(newSubject.verify(password, accountId, passwordDigest)).isFalse()
  }
}
