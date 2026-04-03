package com.wasmo.website

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.OsPage
import com.wasmo.api.routes.RoutingContext
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody

interface ServerOsPage : OsPage {
  val deployment: Deployment
  val response: Response<ResponseBody>

  interface Factory {
    fun create(
      routingContext: RoutingContext,
      accountSnapshot: AccountSnapshot,
      inviteTicket: InviteTicket? = null,
      computerSnapshot: ComputerSnapshot? = null,
      computerListSnapshot: ComputerListSnapshot? = null,
    ): ServerOsPage
  }
}
