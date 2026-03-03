package com.wasmo.calls

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.RoutingContext

/**
 * This is scoped to a single API call or page load, and loads just the data we need for that call.
 */
interface CallDataService {
  context(transactionCallbacks: TransactionCallbacks)
  fun routingContext(): RoutingContext

  context(transactionCallbacks: TransactionCallbacks)
  fun accountSnapshot(): AccountSnapshot

  context(transactionCallbacks: TransactionCallbacks)
  fun computerListSnapshot(): ComputerListSnapshot

  context(transactionCallbacks: TransactionCallbacks)
  fun inviteTicketOrNull(code: String): InviteTicket?

  context(transactionCallbacks: TransactionCallbacks)
  fun computerSnapshotOrNull(slug: ComputerSlug): ComputerSnapshot?

  interface Factory {
    fun create(client: Client): CallDataService
  }
}
