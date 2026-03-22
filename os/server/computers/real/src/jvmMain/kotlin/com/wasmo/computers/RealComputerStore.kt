package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.Computer
import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class RealComputerStore(
  private val wasmoDb: WasmoDb,
  private val computerServiceGraphFactory: ComputerServiceGraph.Factory,
) : ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  override fun initializeFromSpec(computerSpecToken: String): ComputerService {
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

    val result = get(computerId)
    result.initialize()
    return result
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun getOrNull(
    client: Client,
    slug: ComputerSlug,
  ): ComputerService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = wasmoDb.computerQueries.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = slug,
    ).executeAsOneOrNull()
      ?: return null

    return get(computer)
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun get(computerId: ComputerId): ComputerService {
    val computer = wasmoDb.computerQueries.selectComputerById(
      id = computerId,
    ).executeAsOne()

    return get(computer)
  }

  private fun get(computer: Computer): ComputerService =
    computerServiceGraphFactory.create(computer).service
}
