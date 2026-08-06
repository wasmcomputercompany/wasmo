package com.wasmo.passwords

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import com.wasmo.identifiers.HashingPepper
import com.wasmo.identifiers.PasswordDigest

abstract class PasswordHasher(protected val pepper: HashingPepper) {
  /**
   * Computes a salted, peppered cryptographic hash of the given password and accountId,
   * and returns a [PasswordDigest] containing that hash plus relevant metadata (salt,
   * hasing algorithm version used). The structure of the returned String is an implementation
   * detail which clients should treat as opaque.
   *
   * Implementation must securely generate a new random salt on each invocation, and use that
   * salt as well as [pepper] as an input to the hash.
   *
   * Therefore, for values returned by [digest] to later [verify], the same [pepper] value must
   * be specified at construction time and the same PasswordHasher implementation, or a backwards
   * compatible later version, should be used.
   */
  abstract fun digest(password: AccountPassword, accountId: AccountId): PasswordDigest

  /**
   * Checks whether the given password matches the given digest, subject to the digest having
   * been produced by a [PasswordHasher] configured with the same [pepper] value.
   */
  abstract fun verify(
    password: AccountPassword,
    accountId: AccountId,
    passwordDigest: PasswordDigest
  ): Boolean
}

