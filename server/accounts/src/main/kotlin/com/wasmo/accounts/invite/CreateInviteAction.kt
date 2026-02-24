package com.wasmo.accounts.invite

import com.wasmo.accounts.Client
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response

class CreateInviteAction(
  private val client: Client,
  private val routeCodec: RouteCodec,
  private val wasmoDbService: WasmoDbService,
  private val inviteService: InviteService,
) {
  fun create(
    request: CreateInviteRequest,
  ): Response<CreateInviteResponse> {
    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val inviteTicket = inviteService.create(client)
      val inviteRoute = InviteRoute(
        code = inviteTicket.code,
      )
      Response(
        body = CreateInviteResponse(
          inviteUrl = routeCodec.encode(inviteRoute).toHttpUrl().toString(),
        ),
      )
    }
  }
}
