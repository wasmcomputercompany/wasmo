package com.wasmo.computers

import com.wasmo.db.computers.insertComputerSpec
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.OsScope
import com.wasmo.sql.SqlTransaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import org.postgresql.util.PSQLException

@Inject
@SingleIn(OsScope::class)
class ComputerSpecStore(
  private val clock: Clock,
) {
  context(sqlTransaction: SqlTransaction)
  suspend fun insertIfAbsent(
    accountId: AccountId,
    slug: ComputerSlug,
    computerSpecToken: String,
  ) {
    try {
      insertComputerSpec(
        created_at = clock.now(),
        version = 1,
        account_id = accountId,
        token = computerSpecToken,
        slug = slug,
      )
    } catch (e: PSQLException) {
      // TODO: recover from idempotent inserts
      throw e
    }
  }
}
