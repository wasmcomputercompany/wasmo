package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.PasskeySnapshot
import com.wasmo.app.db.WasmoDbService
import com.wasmo.db.Passkey
import com.wasmo.passkeys.AuthenticatorDatabase

class RealAccountStore private constructor(
  private val authenticatorDatabase: AuthenticatorDatabase,
  private val wasmoDbService: WasmoDbService,
  private val client: Client,
) : AccountStore {
  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot(): AccountSnapshot {
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

  class Factory(
    private val authenticatorDatabase: AuthenticatorDatabase,
    private val wasmoDbService: WasmoDbService,
  ) : AccountStore.Factory {
    override fun create(client: Client) = RealAccountStore(
      authenticatorDatabase = authenticatorDatabase,
      wasmoDbService = wasmoDbService,
      client = client,
    )
  }
}
