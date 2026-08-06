package com.wasmo.passwords

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.HashingPepper
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
  fun `successive hashes differ even for the same parameters and same pepper`() {
    val a = subject.digest(password, accountId)
    val b = subject.digest(password, accountId)
    assertThat(a).isNotEqualTo(b)
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
