package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSlug
import com.wasmo.db.Computer
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AccountId
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

  context(_: TransactionCallbacks)
  fun getOrCreateComputer(computerSpecToken: String): Computer {
    val computerSpec = wasmoDb.computerSpecQueries
      .selectComputerSpecByToken(computerSpecToken)
      .executeAsOneOrNull()
      ?: throw IllegalStateException("no such computer spec: $computerSpecToken")

    val computerId = computerSpec.computer_id
      ?: run {
        val insertedComputerId = wasmoDb.computerQueries.insertComputer(
          created_at = computerSpec.created_at,
          version = 1,
          slug = computerSpec.slug,
        ).executeAsOne()

        wasmoDb.computerAccessQueries.insertComputerAccess(
          created_at = computerSpec.created_at,
          version = 1,
          computer_id = insertedComputerId,
          account_id = computerSpec.account_id,
        ).executeAsOne()

        wasmoDb.computerSpecQueries.linkComputer(
          new_version = computerSpec.version + 1,
          computer_id = insertedComputerId,
          expected_version = computerSpec.version,
          id = computerSpec.id,
        )

        insertedComputerId
      }

    return wasmoDb.computerQueries
      .selectComputerById(computerId)
      .executeAsOne()
  }
}
