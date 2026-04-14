package com.wasmo.accounts

import com.wasmo.app.db2.WasmoDbTransaction as TransactionCallbacks

/** Used for actions that don't access user data. */
internal class UnauthenticatedCaller(
  override val userAgent: String?,
  override val ip: String?,
) : Caller {
  context(transactionCallbacks: TransactionCallbacks)
  override suspend fun getAccountIdOrNull() = null
}
