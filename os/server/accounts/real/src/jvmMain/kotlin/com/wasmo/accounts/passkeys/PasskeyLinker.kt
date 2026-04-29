package com.wasmo.accounts.passkeys

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.db.passkeys.DbPasskey
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmox.sql.SqlTransaction

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
  private val client: Client,
) {
  context(sqlTransaction: SqlTransaction)
  suspend fun link(passkey: DbPasskey) {
    val cookieAccountId = client.getOrCreateAccountId()

    // Transfer all cookies.
    client.signIn(
      sourceAccountId = cookieAccountId,
      targetAccountId = passkey.accountId,
    )
  }
}
