package com.wasmo.calls

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import wasmox.sql.SqlTransaction

/**
 * This is scoped to a single API call or page load, and loads just the data we need for that call.
 */
interface CallDataService {
  context(sqlTransaction: SqlTransaction)
  suspend fun routingContext(): RoutingContext

  context(sqlTransaction: SqlTransaction)
  suspend fun routeCodec(): RouteCodec

  context(sqlTransaction: SqlTransaction)
  suspend fun accountSnapshot(): AccountSnapshot

  context(sqlTransaction: SqlTransaction)
  suspend fun computerListSnapshot(): ComputerListSnapshot

  context(sqlTransaction: SqlTransaction)
  suspend fun inviteTicketOrNull(code: String): InviteTicket?
}
