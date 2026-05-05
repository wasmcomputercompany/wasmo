package com.wasmo.accounts.invite

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateInviteRequest
import com.wasmo.api.CreateInviteResponse
import com.wasmo.api.routes.InviteRoute
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.framework.toHttpUrl
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(CreateInviteRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class CreateInviteRpc(
  private val client: Client,
  private val callDataService: CallDataService,
  private val wasmoDb: SqlDatabase,
  private val inviteService: InviteService,
) : RpcAction<CreateInviteRequest, CreateInviteResponse> {
  suspend fun create(
    request: CreateInviteRequest,
  ): Response<CreateInviteResponse> {
    return wasmoDb.transaction {
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

  override suspend fun invoke(
    userAgent: UserAgent,
    request: CreateInviteRequest,
    url: Url,
  ) = create(request)
}
