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
  context(sqlTransaction: SqlTransaction)
  suspend fun setPassword(accountId: AccountId, oldPassword: AccountPassword, newPassword: AccountPassword): Boolean
}
