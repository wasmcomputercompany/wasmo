package com.wasmo.calls

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.app.db2.WasmoDbTransaction as TransactionCallbacks

/**
 * This is scoped to a single API call or page load, and loads just the data we need for that call.
 */
interface CallDataService {
  context(transactionCallbacks: TransactionCallbacks)
  suspend fun routingContext(): RoutingContext

  context(transactionCallbacks: TransactionCallbacks)
  suspend fun routeCodec(): RouteCodec

  context(transactionCallbacks: TransactionCallbacks)
  suspend fun accountSnapshot(): AccountSnapshot

  context(transactionCallbacks: TransactionCallbacks)
  suspend fun computerListSnapshot(): ComputerListSnapshot

  context(transactionCallbacks: TransactionCallbacks)
  suspend fun inviteTicketOrNull(code: String): InviteTicket?
}
