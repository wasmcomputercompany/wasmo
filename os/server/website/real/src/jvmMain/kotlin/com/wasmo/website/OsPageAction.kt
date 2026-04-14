package com.wasmo.website

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.Url
import com.wasmo.app.db.WasmoDb
import com.wasmo.calls.CallDataService
import com.wasmo.computers.ComputerService
import com.wasmo.computers.ComputerStore
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.UnauthorizedUserException
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * We serve the same page to most routes, with different embedded page data.
 */
@Inject
@SingleIn(CallScope::class)
class OsPageAction(
  private val computerStore: ComputerStore,
  private val callDataService: CallDataService,
  private val osPageFactory: ServerOsPage.Factory,
  private val wasmoDb: WasmoDb,
  private val client: Client,
) {
  suspend fun get(url: Url): ServerOsPage {
    var accountSnapshot: AccountSnapshot? = null
    var routingContext: RoutingContext? = null
    var inviteTicket: InviteTicket? = null
    var computerService: ComputerService? = null
    var computerListSnapshot: ComputerListSnapshot? = null

    wasmoDb.transactionWithResult(noEnclosing = true) {
      accountSnapshot = callDataService.accountSnapshot()
      routingContext = callDataService.routingContext()
      val routeCodec = callDataService.routeCodec()

      when (val route = routeCodec.decode(url)) {
        is ComputerHomeRoute -> {
          computerService = computerStore.getOrNull(client, route.slug)
            ?: throw UnauthorizedUserException()
        }

        ComputerListRoute -> {
          computerListSnapshot = callDataService.computerListSnapshot()
        }

        is InviteRoute -> {
          inviteTicket = callDataService.inviteTicketOrNull(route.code)
            ?: throw NotFoundUserException()
        }

        else -> {
        }
      }
    }

    return osPageFactory.create(
      routingContext = routingContext!!,
      accountSnapshot = accountSnapshot!!,
      inviteTicket = inviteTicket,
      computerSnapshot = computerService?.snapshot(),
      computerListSnapshot = computerListSnapshot,
    )
  }
}
