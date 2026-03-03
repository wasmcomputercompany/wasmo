package com.wasmo.website

import com.wasmo.accounts.ClientScope
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.Url
import com.wasmo.calls.CallDataService
import com.wasmo.db.WasmoDb
import com.wasmo.framework.NotFoundException
import com.wasmo.framework.UnauthorizedException
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * We serve the same page to most routes, with different embedded page data.
 */
@Inject
@SingleIn(ClientScope::class)
class HostPageAction(
  private val callDataService: CallDataService,
  private val hostPageFactory: ServerHostPage.Factory,
  private val wasmoDb: WasmoDb,
) {
  fun get(url: Url): ServerHostPage {
    return wasmoDb.transactionWithResult(noEnclosing = true) {
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
