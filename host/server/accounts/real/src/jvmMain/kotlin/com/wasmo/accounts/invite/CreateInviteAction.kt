package com.wasmo.accounts.invite

import com.wasmo.accounts.Client
import com.wasmo.accounts.ClientScope
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ClientScope::class)
class CreateInviteAction(
  private val client: Client,
  private val callDataService: CallDataService,
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
      val routeCodec = callDataService.routeCodec()
      Response(
        body = CreateInviteResponse(
          inviteUrl = routeCodec.encode(inviteRoute).toHttpUrl().toString(),
        ),
      )
    }
  }
}
