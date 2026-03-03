package com.wasmo.website

import com.wasmo.accounts.Client
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.Url
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.CallDataService
import com.wasmo.framework.NotFoundException
import com.wasmo.framework.UnauthorizedException

/**
 * We serve the same page to most routes, with different embedded page data.
 */
class HostPageAction(
  private val client: Client,
  private val callDataServiceFactory: CallDataService.Factory,
  private val hostPageFactory: ServerHostPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(url: Url): ServerHostPage {
    val callDataService = callDataServiceFactory.create(client)

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val accountSnapshot = callDataService.accountSnapshot()
      val routingContext = callDataService.routingContext()
      val routeCodec = callDataService.routeCodec()
      val route = routeCodec.decode(url)

      var inviteTicket: InviteTicket? = null
      var computerSnapshot: ComputerSnapshot? = null
      var computerListSnapshot: ComputerListSnapshot? = null

      when (route) {
        is ComputerHomeRoute -> {
          computerSnapshot = callDataService.computerSnapshotOrNull(route.slug)
            ?: throw UnauthorizedException()
        }

        ComputerListRoute -> {
          computerListSnapshot = callDataService.computerListSnapshot()
        }

        is InviteRoute -> {
          inviteTicket = callDataService.inviteTicketOrNull(route.code)
            ?: throw NotFoundException()
        }

        else -> {
        }
      }

      hostPageFactory.create(
        routingContext = routingContext,
        accountSnapshot = accountSnapshot,
        inviteTicket = inviteTicket,
        computerSnapshot = computerSnapshot,
        computerListSnapshot = computerListSnapshot,
      )
    }
  }
}
