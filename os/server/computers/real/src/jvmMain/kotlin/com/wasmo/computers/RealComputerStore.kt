package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.app.db.Computer
import com.wasmo.sql.SqlTransaction
import com.wasmo.app.db.insertComputer
import com.wasmo.app.db.insertComputerAccess
import com.wasmo.app.db.linkComputer
import com.wasmo.app.db.selectComputerByAccountIdAndSlug
import com.wasmo.app.db.selectComputerById
import com.wasmo.app.db.selectComputerSpecByToken
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class RealComputerStore(
  private val computerServiceGraphFactory: ComputerServiceGraph.Factory,
) : ComputerStore {
  context(sqlTransaction: SqlTransaction)
  override suspend fun initializeFromSpec(computerSpecToken: String): ComputerService {
    val computerSpec = sqlTransaction.selectComputerSpecByToken(computerSpecToken)
      ?: throw IllegalStateException("no such computer spec: $computerSpecToken")

    val computerId = computerSpec.computer_id
      ?: run {
        val insertedComputerId = sqlTransaction.insertComputer(
          created_at = computerSpec.created_at,
          version = 1,
          slug = computerSpec.slug,
        )

        sqlTransaction.insertComputerAccess(
          created_at = computerSpec.created_at,
          version = 1,
          computer_id = insertedComputerId,
          account_id = computerSpec.account_id,
        )

        sqlTransaction.linkComputer(
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

  context(sqlTransaction: SqlTransaction)
  override suspend fun getOrNull(
    client: Client,
    slug: ComputerSlug,
  ): ComputerService? {
    val accountId = client.getAccountIdOrNull()
      ?: return null

    val computer = sqlTransaction.selectComputerByAccountIdAndSlug(
      account_id = accountId,
      slug = slug,
    ) ?: return null

    return get(computer)
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun get(computerId: ComputerId): ComputerService {
    val computer = sqlTransaction.selectComputerById(
      id = computerId,
    )

    return get(computer)
  }

  private fun get(computer: Computer): ComputerService =
    computerServiceGraphFactory.create(computer).service
}
