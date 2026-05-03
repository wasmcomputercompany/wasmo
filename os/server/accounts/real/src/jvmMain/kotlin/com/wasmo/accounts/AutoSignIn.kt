package com.wasmo.accounts

import com.wasmo.api.AccountType
import com.wasmo.db.accounts.insertAccount
import com.wasmo.db.accounts.listAccounts
import com.wasmo.db.passwords.findBlankPasswordsForAccount
import com.wasmo.db.passwords.insertPassword
import com.wasmo.identifiers.AccountId
import kotlin.time.Clock
import wasmo.sql.SqlConnection

fun interface AutoSignIn {
  /**
   * Returns the [accountId] to auto sign-in to, or `null` to not auto sign-in.
   */
  context(connection: SqlConnection)
  suspend fun accountToAutoSignInto(): AccountId?
}

class DoNotAutoSignIn : AutoSignIn {
  context(connection: SqlConnection)
  override suspend fun accountToAutoSignInto(): AccountId? = null
}

class LocalAccountAutoSignIn(
  val clock: Clock,
  val accountType: AccountType,
) : AutoSignIn {
  val INITIAL_USERNAME = "admin"
  /**
   * Returns the accountId of the single local account if
   *   - There were no accounts (then this method creates one, without a password), or
   *   - There is exactly one account, and it doesn't have a password
   * - Else, returns null
   */
  context(connection: SqlConnection)
  override suspend fun accountToAutoSignInto(): AccountId? {
    if (accountType != AccountType.Local) {
      return null
    }
    val accountIds = listAccounts(limit = 2)
    return when (accountIds.size) {
      0 -> {
        val now = clock.now()
        val accountId = insertAccount(version = 1)
        val passwordId = insertPassword(
          createdAt = now,
          accountId = accountId,
          username = INITIAL_USERNAME,
          passwordHash = null,
          createdByUserAgent = null, // TODO: Add CallScope so we can fill-in user-agent and IP
          createdByIp = null,
          active = true,
        )
        accountId
      }
      1 -> {
        val accountId = accountIds.single()
        val passwords = findBlankPasswordsForAccount(accountId, limit = 1)
        if (passwords.isEmpty()) null else accountId
      }
      else -> null
    }
  }
}
