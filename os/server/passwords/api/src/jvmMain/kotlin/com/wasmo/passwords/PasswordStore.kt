package com.wasmo.passwords

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.AccountPassword
import wasmox.sql.SqlTransaction

interface PasswordStore {
  /**
   * Returns true of the provided password matches the password recorded for the given AccountId.
   */
  context(sqlTransaction: SqlTransaction)
  suspend fun checkPassword(accountId: AccountId, providedPassword: AccountPassword): Boolean

  // TODO: Make it possible to enforce password requirements such as minimum length and entropy.
  context(sqlTransaction: SqlTransaction)
  suspend fun setPassword(accountId: AccountId, oldPassword: AccountPassword, newPassword: AccountPassword): Boolean
}
