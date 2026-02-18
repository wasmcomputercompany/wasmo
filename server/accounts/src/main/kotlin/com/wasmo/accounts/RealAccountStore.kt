package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.AccountSnapshot
import com.wasmo.api.PasskeySnapshot
import com.wasmo.db.Passkey
import com.wasmo.db.PasskeyQueries
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.passkeys.Challenger

class RealAccountStore private constructor(
  private val challenger: Challenger,
  private val authenticatorDatabase: AuthenticatorDatabase,
  private val passkeyQueries: PasskeyQueries,
  private val client: Client,
) : AccountStore {
  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot(): AccountSnapshot {
    val accountId = client.getAccountIdOrNull()

    val passkeys = when {
      accountId != null -> passkeyQueries.findPasskeysByAccountId(accountId)
        .executeAsList()
        .map { it.toSnapshot() }

      else -> listOf()
    }

    return AccountSnapshot(
      nextChallenge = challenger.create(),
      passkeys = passkeys,
    )
  }

  private fun Passkey.toSnapshot() = PasskeySnapshot(
    authenticator = authenticatorDatabase.forAaguid(aaguid),
    createdAt = created_at,
  )

  class Factory(
    private val challenger: Challenger,
    private val authenticatorDatabase: AuthenticatorDatabase,
    private val passkeyQueries: PasskeyQueries,
  ) : AccountStore.Factory {
    override fun create(client: Client) = RealAccountStore(
      challenger = challenger,
      authenticatorDatabase = authenticatorDatabase,
      passkeyQueries = passkeyQueries,
      client = client,
    )
  }
}
