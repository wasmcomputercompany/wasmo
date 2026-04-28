package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.db.computers.DbComputer
import com.wasmo.db.computers.insertComputer
import com.wasmo.db.computers.insertComputerAccess
import com.wasmo.db.computers.linkComputer
import com.wasmo.db.computers.selectComputerByAccountIdAndSlug
import com.wasmo.db.computers.selectComputerById
import com.wasmo.db.computers.selectComputerSpecByToken
import com.wasmo.db.computers.selectMaxUserIdFromComputerAccess
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.UserId
import com.wasmo.sql.SqlTransaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class RealComputerStore(
  private val computerServiceGraphFactory: ComputerServiceGraph.Factory,
) : ComputerStore {
  context(sqlTransaction: SqlTransaction)
  override suspend fun initializeFromSpec(computerSpecToken: String): ComputerService {
    val computerSpec = selectComputerSpecByToken(computerSpecToken)
      ?: throw IllegalStateException("no such computer spec: $computerSpecToken")

    val computerId = computerSpec.computerId
      ?: run {
        val insertedComputerId = insertComputer(
          createdAt = computerSpec.createdAt,
          version = 1,
          slug = computerSpec.slug,
        )

        // Note that we know this will always be null, but someday we'll be able to grant access
        // on existing computers and this should handle that too.
        val maxUserId = selectMaxUserIdFromComputerAccess(insertedComputerId)
        val userId = when {
          maxUserId == null -> UserId(1L)
          else -> UserId(maxUserId.id + 1L)
        }

        insertComputerAccess(
          createdAt = computerSpec.createdAt,
          version = 1,
          computerId = insertedComputerId,
          accountId = computerSpec.accountId,
          userId = userId,
        )

        linkComputer(
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

    val computer = selectComputerByAccountIdAndSlug(
      accountId = accountId,
      slug = slug,
    ) ?: return null

    return get(computer)
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun get(computerId: ComputerId): ComputerService {
    val computer = selectComputerById(
      id = computerId,
    )

    return get(computer)
  }

  private fun get(computer: DbComputer): ComputerService =
    computerServiceGraphFactory.create(computer).service
}
