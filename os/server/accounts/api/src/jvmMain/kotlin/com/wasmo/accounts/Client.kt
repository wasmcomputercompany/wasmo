package com.wasmo.accounts

import com.wasmo.app.db.SqlTransaction
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

  context(sqlTransaction: SqlTransaction)
  suspend fun getOrCreateAccountId(): AccountId

  /** Call this when the account ID itself may have changed. */
  context(sqlTransaction: SqlTransaction)
  fun invalidate()
}

abstract class CallScope private constructor()
