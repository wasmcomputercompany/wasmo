package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks

/** Used for actions that don't access user data. */
internal class UnauthenticatedCaller(
  override val userAgent: String?,
  override val ip: String?,
) : Caller {
  context(transactionCallbacks: TransactionCallbacks)
  override fun getAccountIdOrNull() = null
}
