package com.wasmo.accounts

import com.wasmo.app.db2.WasmoDbTransaction
import com.wasmo.identifiers.AccountId

/**
 * A caller represents a user-agent, like a browser or a web crawler.
 *
 * Prefer [Client] for actions that write new data to the database.
 */
interface Caller {
  val userAgent: String?
  val ip: String?

  context(transactionCallbacks: WasmoDbTransaction)
  fun getAccountIdOrNull(): AccountId?
}
