package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks

/** Used for unauthenticated actions that don't access user data. */
internal class UnauthenticatedClient(
  override val userAgent: String?,
  override val ip: String?,
) : Client {
  context(transactionCallbacks: TransactionCallbacks)
  override fun getAccountIdOrNull() = null

  context(transactionCallbacks: TransactionCallbacks)
  override fun getOrCreateAccountId() = error("unexpected Unauthenticated.getOrCreateAccountId")

  context(transactionCallbacks: TransactionCallbacks)
  override fun invalidate() {
  }
}
