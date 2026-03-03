package com.wasmo.website

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.Url
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.CallDataService
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.deployment.Deployment
import com.wasmo.framework.NotFoundException
import com.wasmo.framework.UnauthorizedException

/**
 * We serve the same page to most routes, with different embedded page data.
 */
class HostPageAction(
  private val client: Client,
  private val deployment: Deployment,
  private val callDataServiceFactory: CallDataService.Factory,
  private val hostPageFactory: ServerHostPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(url: Url): ServerHostPage {
    val callDataService = callDataServiceFactory.create(client)

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountSnapshot = callDataService.accountSnapshot()
      val routingContext = RoutingContext(
        rootUrl = deployment.baseUrl.toString(),
        hasComputers = false,
        hasInvite = accountSnapshot.hasInvite,
        isAdmin = false,
      )
      val router = RealRouteCodec(routingContext)
      val route = router.decode(url)

      hostPageFactory.create(
        routingContext = routingContext,
        accountSnapshot = accountSnapshot,
        inviteTicket = loadInviteTicket(route, callDataService),
        computerSnapshot = loadComputerSnapshotOrNull(route, callDataService),
        computerListSnapshot = loadComputerListSnapshotOrNull(route, callDataService),
      )
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun loadComputerListSnapshotOrNull(
    route: Route,
    callDataService: CallDataService,
  ): ComputerListSnapshot? {
    if (route !is ComputerListRoute) return null

    return callDataService.computerListSnapshot()
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun loadInviteTicket(
    route: Route,
    callDataService: CallDataService,
  ): InviteTicket? {
    if (route !is InviteRoute) return null

    return callDataService.inviteTicketOrNull(route.code)
      ?: throw NotFoundException()
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun loadComputerSnapshotOrNull(
    route: Route,
    callDataService: CallDataService,
  ): ComputerSnapshot? {
    if (route !is ComputerHomeRoute) return null

    return callDataService.computerSnapshotOrNull(route.slug)
      ?: throw UnauthorizedException()
  }
}
