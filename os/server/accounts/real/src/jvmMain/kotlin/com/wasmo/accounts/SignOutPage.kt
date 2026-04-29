package com.wasmo.accounts

import com.wasmo.api.routes.HomeRoute
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.redirect
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@SingleIn(CallScope::class)
class SignOutPage(
  private val callDataService: CallDataService,
  private val wasmoDb: SqlDatabase,
  private val client: Client,
) {
  suspend fun get(): Response<ResponseBody> {
    wasmoDb.transaction {
      client.signOut()
    }
    val routeCodec = wasmoDb.transaction {
      callDataService.routeCodec()
    }
    val url = routeCodec.encode(HomeRoute)
    return redirect(url.toHttpUrl())
  }
}
