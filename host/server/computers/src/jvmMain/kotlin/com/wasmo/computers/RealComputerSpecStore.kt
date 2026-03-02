package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSlug
import com.wasmo.app.db.WasmoDbService
import com.wasmo.db.Computer
import com.wasmo.identifiers.AccountId
import kotlin.time.Clock
import org.postgresql.util.PSQLException

class ComputerSpecStore(
  private val clock: Clock,
  private val wasmoDbService: WasmoDbService,
) {
  context(_: TransactionCallbacks)
  fun createSpec(
    accountId: AccountId,
    slug: ComputerSlug,
    computerSpecToken: String,
  ) {
    try {
      wasmoDbService.computerSpecQueries.insertComputerSpec(
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
    val computerSpec = wasmoDbService.computerSpecQueries
      .selectComputerSpecByToken(computerSpecToken)
      .executeAsOneOrNull()
      ?: throw IllegalStateException("no such computer spec: $computerSpecToken")

    val computerId = computerSpec.computer_id
      ?: run {
        val insertedComputerId = wasmoDbService.computerQueries.insertComputer(
          created_at = computerSpec.created_at,
          version = 1,
          slug = computerSpec.slug,
        ).executeAsOne()

        wasmoDbService.computerAccessQueries.insertComputerAccess(
          created_at = computerSpec.created_at,
          version = 1,
          computer_id = insertedComputerId,
          account_id = computerSpec.account_id,
        ).executeAsOne()

        wasmoDbService.computerSpecQueries.linkComputer(
          new_version = computerSpec.version + 1,
          computer_id = insertedComputerId,
          expected_version = computerSpec.version,
          id = computerSpec.id,
        )

        insertedComputerId
      }

    return wasmoDbService.computerQueries
      .selectComputerById(computerId)
      .executeAsOne()
  }
}
