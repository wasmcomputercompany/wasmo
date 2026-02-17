package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.identifiers.AccountId

/**
 * A client represents a user-agent, like a browser or a web crawler.
 *
 * We create database accounts lazily for clients that need persisted data.
 *
 * Multiple clients may share an account. This is typically by sharing passkeys.
 */
interface Client {
  val userAgent: String?
  val ip: String?

  context(transactionCallbacks: TransactionCallbacks)
  fun getAccountIdOrNull(): AccountId?

  context(transactionCallbacks: TransactionCallbacks)
  fun getOrCreateAccountId(): AccountId

  /** Call this when the account ID itself may have changed. */
  context(transactionCallbacks: TransactionCallbacks)
  fun invalidate()
}
