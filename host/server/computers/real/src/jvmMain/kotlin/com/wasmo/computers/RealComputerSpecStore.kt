package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import org.postgresql.util.PSQLException

@Inject
@SingleIn(AppScope::class)
class ComputerSpecStore(
  private val clock: Clock,
  private val wasmoDb: WasmoDb,
) {
  context(_: TransactionCallbacks)
  fun insertIfAbsent(
    accountId: AccountId,
    slug: ComputerSlug,
    computerSpecToken: String,
  ) {
    try {
      wasmoDb.computerSpecQueries.insertComputerSpec(
        created_at = clock.now(),
        version = 1,
        account_id = accountId,
        token = computerSpecToken,
        slug = slug,
      ).executeAsOneOrNull()
    } catch (e: PSQLException) {
      // TODO: recover from idempotent inserts
      throw e
    }
  }
}
