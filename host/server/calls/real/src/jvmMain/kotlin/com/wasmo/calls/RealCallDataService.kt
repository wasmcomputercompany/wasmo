package com.wasmo.calls

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.AppSlug
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSlug
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledApp
import com.wasmo.api.InviteTicket
import com.wasmo.api.PasskeySnapshot
import com.wasmo.app.db.WasmoDbService
import com.wasmo.db.Passkey
import com.wasmo.passkeys.AuthenticatorDatabase

class RealCallDataService private constructor(
  private val authenticatorDatabase: AuthenticatorDatabase,
  private val wasmoDbService: WasmoDbService,
  private val client: Client,
) : CallDataService {
  context(transactionCallbacks: TransactionCallbacks)
  override fun accountSnapshot(): AccountSnapshot {
    val accountId = client.getAccountIdOrNull()

    val passkeys = when {
      accountId != null -> wasmoDbService.passkeyQueries.findPasskeysByAccountId(accountId)
        .executeAsList()
        .map { it.toSnapshot() }

      else -> listOf()
    }

    val inviteOrNull = when {
      accountId != null -> {
        wasmoDbService.inviteQueries.findInvitesByClaimedBy(
          claimed_by = accountId,
          limit = 1,
        ).executeAsOneOrNull()

      }

      else -> null
    }

    return AccountSnapshot(
      nextChallenge = client.challenger.create(),
      passkeys = passkeys,
      hasInvite = inviteOrNull != null,
    )
  }

  private fun Passkey.toSnapshot() = PasskeySnapshot(
    authenticator = authenticatorDatabase.forAaguid(aaguid),
    createdAt = created_at,
  )

  context(transactionCallbacks: TransactionCallbacks)
  override fun computerListSnapshot(): ComputerListSnapshot {
    val accountId = client.getAccountIdOrNull()
      ?: return ComputerListSnapshot()

    val computers = wasmoDbService.computerQueries.selectComputersByAccountId(
      account_id = accountId,
      limit = 100,
    ).executeAsList()

    return ComputerListSnapshot(
      items = computers.map {
        ComputerListItem(it.slug)
      },
    )
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun inviteTicketOrNull(code: String): InviteTicket? {
    val invite = wasmoDbService.inviteQueries.findInvitesByCode(code)
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

    val computer = wasmoDbService.computerQueries.selectComputerByAccountIdAndSlug(
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

  class Factory(
    private val authenticatorDatabase: AuthenticatorDatabase,
    private val wasmoDbService: WasmoDbService,
  ) : CallDataService.Factory {
    override fun create(client: Client) = RealCallDataService(
      authenticatorDatabase = authenticatorDatabase,
      wasmoDbService = wasmoDbService,
      client = client,
    )
  }
}
