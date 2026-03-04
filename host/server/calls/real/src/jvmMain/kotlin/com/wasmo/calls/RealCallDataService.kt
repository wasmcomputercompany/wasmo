package com.wasmo.calls

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.accounts.CallScope
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledApp
import com.wasmo.api.InviteTicket
import com.wasmo.api.PasskeySnapshot
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.db.Invite
import com.wasmo.db.Passkey
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.AuthenticatorDatabase
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(CallScope::class)
class RealCallDataService(
  private val deployment: Deployment,
  private val routeCodecFactory: RouteCodec.Factory,
  private val authenticatorDatabase: AuthenticatorDatabase,
  private val wasmoDb: WasmoDb,
  private val client: Client,
) : CallDataService {
  private val passkeys = object : DbLazy<List<PasskeySnapshot>>() {
    context(transactionCallbacks: TransactionCallbacks)
    override fun load(): List<PasskeySnapshot> {
      val accountId = client.getAccountIdOrNull()

      return when {
        accountId != null -> wasmoDb.passkeyQueries.findPasskeysByAccountId(accountId)
          .executeAsList()
          .map { it.toSnapshot() }

        else -> listOf()
      }
    }

    private fun Passkey.toSnapshot() = PasskeySnapshot(
      authenticator = authenticatorDatabase.forAaguid(aaguid),
      createdAt = created_at,
    )
  }

  private val firstClaimedInvite = object : DbLazy<Invite?>() {
    context(transactionCallbacks: TransactionCallbacks)
    override fun load(): Invite? {
      val accountId = client.getAccountIdOrNull()
      return when {
        accountId != null -> {
          wasmoDb.inviteQueries.findInvitesByClaimedBy(
            claimed_by = accountId,
            limit = 1,
          ).executeAsOneOrNull()
        }

        else -> null
      }
    }
  }

  private val routingContext = object : DbLazy<RoutingContext>() {
    context(transactionCallbacks: TransactionCallbacks)
    override fun load() = RoutingContext(
      rootUrl = deployment.baseUrl.toString(),
      hasComputers = computerListSnapshot.get().items.isNotEmpty(),
      hasInvite = firstClaimedInvite.get() != null,
      isAdmin = false,
    )
  }

  private val accountSnapshot = object : DbLazy<AccountSnapshot>() {
    context(transactionCallbacks: TransactionCallbacks)
    override fun load(): AccountSnapshot {
      val passkeys = passkeys.get()
      val firstInvite = firstClaimedInvite.get()

      return AccountSnapshot(
        nextChallenge = client.challenger.create(),
        passkeys = passkeys,
        hasInvite = firstInvite != null,
      )
    }
  }

  private val computerListSnapshot = object : DbLazy<ComputerListSnapshot>() {
    context(transactionCallbacks: TransactionCallbacks)
    override fun load(): ComputerListSnapshot {
      val accountId = client.getAccountIdOrNull()
        ?: return ComputerListSnapshot()

      val computers = wasmoDb.computerQueries.selectComputersByAccountId(
        account_id = accountId,
        limit = 100,
      ).executeAsList()

      return ComputerListSnapshot(
        items = computers.map {
          ComputerListItem(it.slug)
        },
      )
    }
  }


  context(transactionCallbacks: TransactionCallbacks)
  override fun routingContext() = routingContext.get()

  context(transactionCallbacks: TransactionCallbacks)
  override fun routeCodec() = routeCodecFactory.create(routingContext())

  context(transactionCallbacks: TransactionCallbacks)
  override fun accountSnapshot() = accountSnapshot.get()

  context(transactionCallbacks: TransactionCallbacks)
  override fun computerListSnapshot() = computerListSnapshot.get()

  context(transactionCallbacks: TransactionCallbacks)
  override fun inviteTicketOrNull(code: String): InviteTicket? {
    val invite = wasmoDb.inviteQueries.findInvitesByCode(code)
      .executeAsOneOrNull()
      ?: return null

    return InviteTicket(
      claimed = invite.claimed_by != null,
      code = invite.code,
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun computerSnapshotOrNull(slug: ComputerSlug): ComputerSnapshot? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = wasmoDb.computerQueries.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = slug,
    ).executeAsOneOrNull()
      ?: return null

    return ComputerSnapshot(
      slug = computer.slug,
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
