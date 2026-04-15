package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.app.db.Computer
import com.wasmo.app.db.WasmoDb
import com.wasmo.app.db2.WasmoDbTransaction as TransactionCallbacks
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class RealComputerStore(
  private val wasmoDb: WasmoDb,
  private val computerServiceGraphFactory: ComputerServiceGraph.Factory,
) : ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  override suspend fun initializeFromSpec(computerSpecToken: String): ComputerService {
    val computerSpec = transactionCallbacks.computerSpecQueries
      .selectComputerSpecByToken(computerSpecToken)
      .executeAsOneOrNull()
      ?: throw IllegalStateException("no such computer spec: $computerSpecToken")

    val computerId = computerSpec.computer_id
      ?: run {
        val insertedComputerId = transactionCallbacks.computerQueries.insertComputer(
          created_at = computerSpec.created_at,
          version = 1,
          slug = computerSpec.slug,
        ).executeAsOne()

        transactionCallbacks.computerAccessQueries.insertComputerAccess(
          created_at = computerSpec.created_at,
          version = 1,
          computer_id = insertedComputerId,
          account_id = computerSpec.account_id,
        )

        transactionCallbacks.computerSpecQueries.linkComputer(
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
  override suspend fun getOrNull(
    client: Client,
    slug: ComputerSlug,
  ): ComputerService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = transactionCallbacks.computerQueries.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = slug,
    ) ?: return null

    return get(computer)
  }

  context(transactionCallbacks: TransactionCallbacks)
  override suspend fun get(computerId: ComputerId): ComputerService {
    val computer = transactionCallbacks.computerQueries.selectComputerById(
      id = computerId,
    ).executeAsOne()

    return get(computer)
  }

  private fun get(computer: Computer): ComputerService =
    computerServiceGraphFactory.create(computer).service
}
