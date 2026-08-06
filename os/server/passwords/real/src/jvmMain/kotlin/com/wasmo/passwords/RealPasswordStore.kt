package com.wasmo.passwords

import com.wasmo.db.passwords.findPasswordDigestForAccount
import com.wasmo.db.passwords.insertPasswordDigest
import com.wasmo.db.passwords.removePasswordDigest
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import kotlin.time.Clock
import wasmox.sql.SqlTransaction

/**
 * Note: This class doesn't apply rate limiting. Rate limiting should be applied at the RPC layer.
 *
 * @param clock time source for creation time of password entries in DB
 * @param passwordHasher produces and verifies [com.wasmo.identifiers.PasswordDigest]s.
 */
class RealPasswordStore(
  private val clock: Clock,
  private val passwordHasher: PasswordHasher,
) : PasswordStore {
  private val dummyMissingPasswordDigest = passwordHasher.digest(AccountPassword.EMPTY, AccountId(-1))

  context(sqlTransaction: SqlTransaction)
  override suspend fun checkPassword(
    accountId: AccountId,
    providedPassword: AccountPassword,
  ): Boolean {
    val dbPasswordDigest = findPasswordDigestForAccount(accountId)

    return if (providedPassword.isNotEmpty() && dbPasswordDigest != null) {
      passwordHasher.verify(
        password = providedPassword,
        accountId = accountId,
        passwordDigest = dbPasswordDigest.passwordDigest,
      )
    } else {
      val isPasswordCorrect = (providedPassword.isEmpty() && dbPasswordDigest == null)
      // waste some cycles to help thwart timing based oracle attacks trying to figure out if an account exists.
      // It's important that we ignore the result because the verify() used dummy values.
      val ignored = passwordHasher.verify(providedPassword, accountId, dummyMissingPasswordDigest)
      isPasswordCorrect
    }
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun setPassword(
    accountId: AccountId,
    oldPassword: AccountPassword,
    newPassword: AccountPassword,
  ): Boolean {
    val newPasswordDigest = passwordHasher.digest(newPassword, accountId)
    val authenticationSucceeded = checkPassword(accountId, oldPassword)
    if (authenticationSucceeded) {
      // If we get here, the caller knows the password (or knows that there is none), so
      // we don't have to worry about constant-time execution anymore.
      // Note: Because we're in a transaction, deleting the old password first is safe:
      // If setting the new password throws, the transaction will be aborted so we don't end
      // up with a password-less account.
      if (oldPassword.isNotEmpty()) {
        removePasswordDigest(accountId)
      }
      // An empty password in memory corresponds to an absent DbPassword in the DB.
      if (newPassword.isNotEmpty()) {
        insertPasswordDigest(clock, accountId, newPasswordDigest)
      }
    }
    return authenticationSucceeded
  }
}
