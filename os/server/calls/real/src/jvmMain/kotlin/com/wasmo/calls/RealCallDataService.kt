package com.wasmo.calls

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.PasskeySnapshot
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.app.db.Invite
import com.wasmo.app.db.Passkey
import com.wasmo.app.db.SqlTransaction
import com.wasmo.app.db.findInvitesByClaimedBy
import com.wasmo.app.db.findInvitesByCode
import com.wasmo.app.db.findPasskeysByAccountId
import com.wasmo.app.db.selectComputersByAccountId
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
  private val client: Client,
) : CallDataService {
  private val passkeys = object : DbLazy<List<PasskeySnapshot>>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): List<PasskeySnapshot> {
      val accountId = client.getAccountIdOrNull()

      return when {
        accountId != null -> sqlTransaction.findPasskeysByAccountId(accountId)
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
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): Invite? {
      val accountId = client.getAccountIdOrNull()
      return when {
        accountId != null -> {
          sqlTransaction.findInvitesByClaimedBy(
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
      hasComputers = computerListSnapshot.get().items.isNotEmpty(),
      hasInvite = firstClaimedInvite.get() != null,
      isAdmin = false,
    )
  }

  private val accountSnapshot = object : DbLazy<AccountSnapshot>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): AccountSnapshot {
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
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): ComputerListSnapshot {
      val accountId = client.getAccountIdOrNull()
        ?: return ComputerListSnapshot()

      val computers = sqlTransaction.selectComputersByAccountId(
        account_id = accountId,
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
  override suspend fun routingContext() = routingContext.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun routeCodec() = routeCodecFactory.create(routingContext())

  context(sqlTransaction: SqlTransaction)
  override suspend fun accountSnapshot() = accountSnapshot.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun computerListSnapshot() = computerListSnapshot.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun inviteTicketOrNull(code: String): InviteTicket? {
    val invite = sqlTransaction.findInvitesByCode(code)
      ?: return null

    return InviteTicket(
      claimed = invite.claimed_by != null,
      code = invite.code,
    )
  }
}
