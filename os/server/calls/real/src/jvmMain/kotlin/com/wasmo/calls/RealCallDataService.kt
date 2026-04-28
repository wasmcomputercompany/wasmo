package com.wasmo.calls

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.LinkedEmailAddressSnapshot
import com.wasmo.api.PasskeySnapshot
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.db.accounts.invite.Invite
import com.wasmo.db.accounts.invite.findInvitesByClaimedBy
import com.wasmo.db.accounts.invite.findInvitesByCode
import com.wasmo.db.computers.selectComputersByAccountId
import com.wasmo.db.emails.LinkedEmailAddress
import com.wasmo.db.emails.findLinkedEmailAddresses
import com.wasmo.db.passkeys.Passkey
import com.wasmo.db.passkeys.findPasskeysByAccountId
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.sql.SqlTransaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(CallScope::class)
class RealCallDataService(
  private val deployment: Deployment,
  private val routeCodecFactory: RouteCodec.Factory,
  private val authenticatorDatabase: AuthenticatorDatabase,
  private val client: Client,
) : CallDataService, Client.Listener {
  init {
    client.addListener(this)
  }

  private val passkeys = object : DbLazy<List<PasskeySnapshot>>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): List<PasskeySnapshot> {
      val accountId = client.getAccountIdOrNull()

      return when {
        accountId != null -> findPasskeysByAccountId(accountId)
          .map { it.toSnapshot() }

        else -> listOf()
      }
    }

    private fun Passkey.toSnapshot() = PasskeySnapshot(
      authenticator = authenticatorDatabase.forAaguid(aaguid),
      createdAt = created_at,
    )
  }

  private val linkedEmailAddresses = object : DbLazy<List<LinkedEmailAddressSnapshot>>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): List<LinkedEmailAddressSnapshot> {
      val accountId = client.getAccountIdOrNull()

      return when {
        accountId != null -> findLinkedEmailAddresses(accountId)
          .map { it.toSnapshot() }

        else -> listOf()
      }
    }

    private fun LinkedEmailAddress.toSnapshot() = LinkedEmailAddressSnapshot(
      linkedAt = createdAt,
      emailAddress = emailAddress,
    )
  }

  private val firstClaimedInvite = object : DbLazy<Invite?>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): Invite? {
      val accountId = client.getAccountIdOrNull()
      return when {
        accountId != null -> {
          findInvitesByClaimedBy(
            claimed_by = accountId,
            limit = 1,
          )
        }

        else -> null
      }
    }
  }

  private val routingContext = object : DbLazy<RoutingContext>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load() = RoutingContext(
      rootUrl = deployment.baseUrl.toString(),
    )
  }

  private val accountSnapshot = object : DbLazy<AccountSnapshot>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): AccountSnapshot {
      val firstInvite = firstClaimedInvite.get()

      return AccountSnapshot(
        nextChallenge = client.challenger.create(),
        passkeys = passkeys.get(),
        emailAddresses = linkedEmailAddresses.get(),
        hasInvite = firstInvite != null,
      )
    }
  }

  private val computerListSnapshot = object : DbLazy<ComputerListSnapshot>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): ComputerListSnapshot {
      val accountId = client.getAccountIdOrNull()
        ?: return ComputerListSnapshot()

      val computers = selectComputersByAccountId(
        accountId = accountId,
        limit = 100,
      )

      return ComputerListSnapshot(
        items = computers.map {
          ComputerListItem(it.slug)
        },
      )
    }
  }

  context(sqlTransaction: SqlTransaction)
  override fun onInvalidate() {
    passkeys.invalidate()
    firstClaimedInvite.invalidate()
    routingContext.invalidate()
    accountSnapshot.invalidate()
    computerListSnapshot.invalidate()
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun routingContext() = routingContext.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun routeCodec() = routeCodecFactory.create(routingContext())

  context(sqlTransaction: SqlTransaction)
  override suspend fun accountSnapshot() = accountSnapshot.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun computerListSnapshot() = computerListSnapshot.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun inviteTicketOrNull(code: String): InviteTicket? {
    val invite = findInvitesByCode(code)
      ?: return null

    return InviteTicket(
      claimed = invite.claimed_by != null,
      code = invite.code,
    )
  }
}
