package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.routes.AppRoute
import com.wasmo.api.routes.toWasmoUrl
import com.wasmo.calls.CallDataService
import com.wasmo.db.WasmoDb
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Figure the right [InstalledAppService] to call, and call it.
 */
@Inject
@SingleIn(CallScope::class)
class CallAppAction(
  private val callDataService: CallDataService,
  private val client: Client,
  private val computerStore: ComputerStore,
  private val wasmoDb: WasmoDb,
) {
  suspend fun call(request: Request): Response<ResponseBody> {
    val installedApp = wasmoDb.transactionWithResult(noEnclosing = true) {
      val routeCodec = callDataService.routeCodec()
      val route = routeCodec.decode(request.url.toWasmoUrl()) as AppRoute

      val computerService = computerStore.getOrNull(client, route.computerSlug)
        ?: throw NotFoundUserException()

      computerService.installedAppOrNull(route.appSlug)
        ?: throw NotFoundUserException()
    }

    return installedApp.call(request)
  }
}
