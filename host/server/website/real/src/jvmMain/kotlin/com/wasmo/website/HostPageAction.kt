package com.wasmo.website

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.AccountStore
import com.wasmo.accounts.Client
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledApp
import com.wasmo.api.InviteTicket
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.api.routes.InviteRoute
import com.wasmo.api.routes.Route
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.routes.Url
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.deployment.Deployment
import com.wasmo.framework.NotFoundException

/**
 * We serve the same page to most routes, with different embedded page data.
 */
class HostPageAction(
  private val client: Client,
  private val deployment: Deployment,
  private val accountStoreFactory: AccountStore.Factory,
  private val hostPageFactory: ServerHostPage.Factory,
  private val wasmoDbService: WasmoDbService,
) {
  fun get(url: Url): ServerHostPage {
    val accountStore = accountStoreFactory.create(client)

    return wasmoDbService.transactionWithResult(noEnclosing = true) {
      val routingContext = RoutingContext(
        rootUrl = deployment.baseUrl.toString(),
        hasComputers = false,
        hasInvite = false,
        isAdmin = false,
      )
      val router = RealRouteCodec(routingContext)
      val route = router.decode(url)

      val accountSnapshot = accountStore.snapshot()
      hostPageFactory.create(
        routingContext = routingContext,
        accountSnapshot = accountSnapshot,
        inviteTicket = loadInviteTicket(route),
        computerSnapshot = loadComputerSnapshotOrNull(route),
        computerListSnapshot = loadComputerListSnapshotOrNull(route),
      )
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun loadComputerListSnapshotOrNull(route: Route): ComputerListSnapshot? {
    if (route !is ComputerListRoute) return null

    return ComputerListSnapshot(
      items = listOf(
        ComputerListItem(
          slug = ComputerSlug("jesse99"),
        ),
        ComputerListItem(
          slug = ComputerSlug("rounds"),
        ),
      ),
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun loadInviteTicket(route: Route): InviteTicket? {
    if (route !is InviteRoute) return null

    val invite = wasmoDbService.inviteQueries.findInvitesByCode(route.code)
      .executeAsOneOrNull()
      ?: throw NotFoundException()

    return InviteTicket(
      claimed = invite.claimed_by != null,
      code = invite.code,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  fun loadComputerSnapshotOrNull(route: Route): ComputerSnapshot? {
    if (route !is ComputerHomeRoute) return null

    return ComputerSnapshot(
      slug = route.slug,
      apps = listOf(
        InstalledApp(
          label = "Files",
          slug = AppSlug("files"),
          maskableIconUrl = "/assets/launcher/sample-folder.svg",
        ),
        InstalledApp(
          label = "Library",
          slug = AppSlug("library"),
          maskableIconUrl = "/assets/launcher/sample-books.svg",
        ),
        InstalledApp(
          label = "Music",
          slug = AppSlug("music"),
          maskableIconUrl = "/assets/launcher/sample-headphones.svg",
        ),
        InstalledApp(
          label = "Photos",
          slug = AppSlug("photos"),
          maskableIconUrl = "/assets/launcher/sample-camera.svg",
        ),
        InstalledApp(
          label = "Pink Journal",
          slug = AppSlug("pink"),
          maskableIconUrl = "/assets/launcher/sample-flower.svg",
        ),
        InstalledApp(
          label = "Recipes",
          slug = AppSlug("recipes"),
          maskableIconUrl = "/assets/launcher/sample-pancakes.svg",
        ),
        InstalledApp(
          label = "Smart Home",
          slug = AppSlug("smart"),
          maskableIconUrl = "/assets/launcher/sample-home.svg",
        ),
        InstalledApp(
          label = "Snake",
          slug = AppSlug("snake"),
          maskableIconUrl = "/assets/launcher/sample-snake.svg",
        ),
        InstalledApp(
          label = "Writer",
          slug = AppSlug("writer"),
          maskableIconUrl = "/assets/launcher/sample-w.svg",
        ),
        InstalledApp(
          label = "Zap",
          slug = AppSlug("zap"),
          maskableIconUrl = "/assets/launcher/sample-z.svg",
        ),
      ),
    )
  }
}
