package com.wasmo.passkeys

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.CookieQueries
import com.wasmo.db.Passkey

/**
 * After we authenticate a passkey we have two accounts for one person.
 *
 *  * The cookie account
 *  * The passkey account
 *
 * Take the following actions:
 *
 *  * Reassign all the account's cookies to the passkey's account
 *
 * Note that we *do not* move other passkeys, in case the account already has a passkey.
 */
class PasskeyLinker private constructor(
  private val cookieQueries: CookieQueries,
  private val client: Client,
) {
  context(transactionCallbacks: TransactionCallbacks)
  fun link(passkey: Passkey) {
    val cookieAccountId = client.getOrCreateAccountId()

    // Nothing to do.
    if (cookieAccountId == passkey.account_id) return

    // Transfer all cookies.
    cookieQueries.updateAccountIdByAccountId(
      target_account_id = passkey.account_id,
      source_account_id = cookieAccountId,
    )

    client.invalidate()
  }

  class Factory(
    private val cookieQueries: CookieQueries,
  ) {
    fun create(client: Client) = PasskeyLinker(
      cookieQueries = cookieQueries,
      client = client,
    )
  }
}
