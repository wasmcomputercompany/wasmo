package com.wasmo.installedapps

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.toWasmoUrl
import com.wasmo.calls.CallDataService
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.sql.transaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase

/**
 * Figure the right [InstalledAppHttpService] to call, and call it as the right caller.
 */
@Inject
@SingleIn(CallScope::class)
class CallAppAction(
  private val callDataService: CallDataService,
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val installedAppStore: InstalledAppStore,
) {
  suspend fun call(request: Request): Response<ResponseBody> {
    val (httpService, caller) = wasmoDb.transaction {
      val routeCodec = callDataService.routeCodec()
      val route = routeCodec.decode(request.url.toWasmoUrl()) as AppRoute
      installedAppStore.getHttpServiceAndAccessOrNull(
        client = client,
        computerSlug = route.computerSlug,
        appSlug = route.appSlug,
      ) ?: throw NotFoundUserException()
    }

    return httpService.execute(caller, request)
  }
}
