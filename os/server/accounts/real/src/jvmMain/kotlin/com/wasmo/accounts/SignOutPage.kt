package com.wasmo.accounts

import com.wasmo.api.routes.HomeRoute
import com.wasmo.calls.CallDataService
import com.wasmo.framework.HttpAction
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.framework.redirect
import com.wasmo.framework.toHttpUrl
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(SignOutPage::class)
@ContributesIntoMap(CallScope::class)
class SignOutPage(
  private val callDataService: CallDataService,
  private val wasmoDb: SqlDatabase,
  private val client: Client,
) : HttpAction {
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

  override suspend fun invoke(
    userAgent: UserAgent,
    url: Url,
    request: Request,
  ) = get()
}
