package com.wasmo.website

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.SignInSnapshot
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.SignInRoute
import com.wasmo.calls.CallDataService
import com.wasmo.computers.ComputerService
import com.wasmo.computers.ComputerStore
import com.wasmo.framework.HttpAction
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.UnauthorizedUserException
import com.wasmo.framework.Url
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

/**
 * We serve the same page to most routes, with different embedded page data.
 */
@Inject
@ClassKey
@ContributesIntoMap(CallScope::class)
class OsPage(
  private val computerStore: ComputerStore,
  private val callDataService: CallDataService,
  private val osHtmlFactory: ServerOsHtml.Factory,
  private val wasmoDb: SqlDatabase,
  private val client: Client,
) : HttpAction {
  suspend fun get(url: Url): ServerOsHtml {
    var accountSnapshot: AccountSnapshot? = null
    var routingContext: RoutingContext? = null
    var inviteTicket: InviteTicket? = null
    var computerService: ComputerService? = null
    var computerListSnapshot: ComputerListSnapshot? = null
    var signInSnapshot: SignInSnapshot? = null

    wasmoDb.transaction {
      accountSnapshot = callDataService.accountSnapshot()
      routingContext = callDataService.routingContext()
      val routeCodec = callDataService.routeCodec()
      val route = routeCodec.decode(url)

      // Home can also handle sign-in as a Single-Page Application, so it needs signInSnapshot.
      // TODO: Let SignUpRoute handle sign-in options regardless of whether a username exists.
      if (route is HomeRoute || route is SignInRoute) {
        // TODO: Make SignInSnapshot responsible for all sign-in options, not only username.
        signInSnapshot = callDataService.signInSnapshot().takeIf { it.usernameOptions.isNotEmpty() }
      }
      when (route) {
        is ComputerHomeRoute -> {
          computerService = computerStore.getOrNull(client, route.slug)
            ?: throw UnauthorizedUserException()
        }

        HomeRoute -> {
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

    return osHtmlFactory.create(
      routingContext = routingContext!!,
      accountSnapshot = accountSnapshot!!,
      inviteTicket = inviteTicket,
      computerSnapshot = computerService?.snapshot(),
      computerListSnapshot = computerListSnapshot,
      signInSnapshot = signInSnapshot,
    )
  }

  override suspend operator fun invoke(
    url: Url,
    request: Request,
  ) = get(url).response
}
