package com.wasmo.installedapps

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.routes.AppRoute
import com.wasmo.calls.CallDataService
import com.wasmo.framework.HttpAction
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.Url
import com.wasmo.framework.toWasmoUrl
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

/**
 * Figure the right [InstalledAppHttpService] to call, and call it as the right caller.
 */
@Inject
@ClassKey(CallAppAction::class)
@ContributesIntoMap(CallScope::class)
class CallAppAction(
  private val callDataService: CallDataService,
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val installedAppStore: InstalledAppStore,
) : HttpAction {
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

  override suspend operator fun invoke(
    url: Url,
    request: Request,
  ) = call(request)
}
