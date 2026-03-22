package com.wasmo.accounts.passkeys

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.db.CookieQueries
import com.wasmo.db.Passkey
import com.wasmo.db.WasmoDb
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

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
@Inject
@SingleIn(CallScope::class)
class PasskeyLinker(
  private val wasmoDb: WasmoDb,
  private val client: Client,
) {
  private val cookieQueries: CookieQueries
    get() = wasmoDb.cookieQueries

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
}
