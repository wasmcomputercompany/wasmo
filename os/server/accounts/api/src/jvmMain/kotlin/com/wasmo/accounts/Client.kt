package com.wasmo.accounts

import com.wasmo.identifiers.AccountId
import com.wasmo.sql.SqlTransaction

/**
 * A caller that may have associated server-side data.
 *
 * We create database accounts lazily for clients that need persisted data.
 *
 * Multiple clients may share an account. This is typically by sharing passkeys or signing in to the
 * same email address.
 */
interface Client : Caller {
  val challenger: Challenger

  context(sqlTransaction: SqlTransaction)
  suspend fun getOrCreateAccountId(): AccountId

  /** Switch the client from [sourceAccountId] to [targetAccountId]. */
  context(sqlTransaction: SqlTransaction)
  suspend fun signIn(
    sourceAccountId: AccountId,
    targetAccountId: AccountId,
  )

  /**
   * Disconnect the current cookie from the account ID it's linked to.
   *
   * This leaves the cookie in-place, to potentially someday support user-friendly features like
   * suggested accounts to sign back in.
   */
  context(sqlTransaction: SqlTransaction)
  suspend fun signOut()

  /** Call this when the account ID itself may have changed. */
  context(sqlTransaction: SqlTransaction)
  fun invalidate()

  fun addListener(listener: Listener)

  interface Listener {
    /** Invalidate in-memory caches if the signed-in account ID changes mid-call. */
    context(sqlTransaction: SqlTransaction)
    fun onInvalidate()
  }
}

abstract class CallScope private constructor()
