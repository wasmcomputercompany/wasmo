package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.identifiers.AccountId

/**
 * A caller that may have associated server-side data.
 *
 * We create database accounts lazily for clients that need persisted data.
 *
 * Multiple clients may share an account. This is typically by sharing passkeys.
 */
interface Client : Caller {
  val challenger: Challenger

  context(transactionCallbacks: TransactionCallbacks)
  fun getOrCreateAccountId(): AccountId

  /** Call this when the account ID itself may have changed. */
  context(transactionCallbacks: TransactionCallbacks)
  fun invalidate()
}

abstract class CallScope private constructor()
