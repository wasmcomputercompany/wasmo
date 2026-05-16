package com.wasmo.calls

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.InviteTicket
import com.wasmo.api.SignInSnapshot
import com.wasmo.api.LinkedEmailAddressSnapshot
import com.wasmo.api.LinkedUsernameSnapshot
import com.wasmo.api.PasskeySnapshot
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.db.accounts.invite.DbInvite
import com.wasmo.db.accounts.invite.findInvitesByClaimedBy
import com.wasmo.db.accounts.invite.findInvitesByCode
import com.wasmo.db.computers.selectComputersByAccountId
import com.wasmo.db.emails.DbLinkedEmailAddress
import com.wasmo.db.emails.findLinkedEmailAddresses
import com.wasmo.db.passkeys.DbPasskey
import com.wasmo.db.passkeys.findPasskeysByAccountId
import com.wasmo.db.usernames.DbUsername
import com.wasmo.db.usernames.findUsernamesThatCanSignIn
import com.wasmo.db.usernames.findUsernameOrNull
import com.wasmo.identifiers.Deployment
import com.wasmo.passkeys.AuthenticatorDatabase
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmox.sql.SqlTransaction

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

    private fun DbPasskey.toSnapshot() = PasskeySnapshot(
      authenticator = authenticatorDatabase.forAaguid(aaguid),
      createdAt = createdAt,
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

    private fun DbLinkedEmailAddress.toSnapshot() = LinkedEmailAddressSnapshot(
      linkedAt = createdAt,
      emailAddress = emailAddress,
    )
  }

  private val linkedUsername = object : DbLazy<LinkedUsernameSnapshot?>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): LinkedUsernameSnapshot? {
      val accountId = client.getAccountIdOrNull()
      return when {
        accountId != null -> findUsernameOrNull(accountId)?.toSnapshot()
        else -> null
      }
    }

    private fun DbUsername.toSnapshot() = LinkedUsernameSnapshot(
      linkedAt = createdAt,
      username = username,
    )
  }

  private val firstClaimedInvite = object : DbLazy<DbInvite?>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): DbInvite? {
      val accountId = client.getAccountIdOrNull()
      return when {
        accountId != null -> {
          findInvitesByClaimedBy(
            claimedBy = accountId,
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

      val emailAddresses = linkedEmailAddresses.get()
      return AccountSnapshot(
        nextChallenge = client.challenger.create(),
        passkeys = passkeys.get(),
        emailAddresses = emailAddresses,
        username = linkedUsername.get(),
        hasInvite = firstInvite != null,
        signedInAccountName = linkedUsername.get()?.username?.value
          ?: emailAddresses.firstOrNull()?.emailAddress
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

  private val signInSnapshot = object : DbLazy<SignInSnapshot>() {
    context(sqlTransaction: SqlTransaction)
    override suspend fun load(): SignInSnapshot =
      SignInSnapshot(
        usernameOptions = findUsernamesThatCanSignIn().map { it.username },
        canCreateUsername = true,
      )
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
  override suspend fun signInSnapshot() = signInSnapshot.get()

  context(sqlTransaction: SqlTransaction)
  override suspend fun inviteTicketOrNull(code: String): InviteTicket? {
    val invite = findInvitesByCode(code)
      ?: return null

    return InviteTicket(
      claimed = invite.claimedBy != null,
      code = invite.code,
    )
  }
}
