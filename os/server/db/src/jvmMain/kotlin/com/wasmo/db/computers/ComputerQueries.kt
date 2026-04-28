package com.wasmo.db.computers

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindComputerId
import com.wasmo.db.bindComputerSlug
import com.wasmo.db.bindUserId
import com.wasmo.db.getAccountId
import com.wasmo.db.getComputerAccessId
import com.wasmo.db.getComputerAccessIdOrNull
import com.wasmo.db.getComputerId
import com.wasmo.db.getComputerSlug
import com.wasmo.db.getUserId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.UserId
import com.wasmo.sql.list
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

context(connection: SqlConnection)
suspend fun insertComputer(
  createdAt: Instant,
  version: Long,
  slug: ComputerSlug,
): ComputerId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO Computer(
      created_at,
      version,
      slug
    )
    VALUES (
      $1,
      $2,
      $3
    ) RETURNING id
    """,
  ) {
    bindInstant(0, createdAt)
    bindS64(1, version)
    bindComputerSlug(2, slug)
  }

  return rowIterator.single {
    getComputerId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectMaxUserIdFromComputerAccess(
  computerId: ComputerId,
): UserId? {
  val rowIterator = connection.executeQuery(
    """
    SELECT user_id
    FROM ComputerAccess
    WHERE computer_id = $1
    ORDER BY user_id DESC
    LIMIT 1
    """,
  ) {
    bindComputerId(0, computerId)
  }

  return rowIterator.singleOrNull {
    getUserId(0)
  }
}

context(connection: SqlConnection)
suspend fun insertComputerAccess(
  createdAt: Instant,
  version: Int,
  computerId: ComputerId,
  accountId: AccountId,
  userId: UserId,
): ComputerAccessId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO ComputerAccess(
      created_at,
      version,
      computer_id,
      account_id,
      user_id
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5
    ) RETURNING id
    """,
  ) {
    bindInstant(0, createdAt)
    bindS32(1, version)
    bindComputerId(2, computerId)
    bindAccountId(3, accountId)
    bindUserId(4, userId)
  }

  return rowIterator.single {
    getComputerAccessId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectComputersByAccountId(
  accountId: AccountId,
  limit: Long,
): List<DbComputer> {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      $computerColumns
    FROM
      ComputerAccess ca,
      Computer c
    WHERE
      c.id = ca.computer_id AND
      ca.account_id = $1
    ORDER BY
      c.slug
    LIMIT $2
    """,
  ) {
    bindAccountId(0, accountId)
    bindS64(1, limit)
  }

  return rowIterator.list {
    getComputer()
  }
}

context(connection: SqlConnection)
suspend fun selectComputerByAccountIdAndSlug(
  accountId: AccountId,
  slug: ComputerSlug,
): DbComputer? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      $computerColumns
    FROM
      ComputerAccess ca,
      Computer c
    WHERE
      c.id = ca.id AND
      ca.account_id = $1 AND
      c.slug = $2
    LIMIT 1
    """,
  ) {
    bindAccountId(0, accountId)
    bindComputerSlug(1, slug)
  }

  return rowIterator.singleOrNull {
    getComputer()
  }
}

context(connection: SqlConnection)
suspend fun selectComputerById(
  id: ComputerId,
): DbComputer {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      $computerColumns
    FROM
      Computer c
    WHERE
      id = $1
    LIMIT 1
    """,
  ) {
    bindComputerId(0, id)
  }

  return rowIterator.single {
    getComputer()
  }
}

context(connection: SqlConnection)
suspend fun selectComputerAndComputerAccess(
  accountId: AccountId,
  slug: ComputerSlug,
): Pair<DbComputer, DbComputerAccess?>? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      $computerColumns,
      $computerAccessColumns
    FROM Computer c
    LEFT JOIN ComputerAccess ca ON (c.id = ca.computer_id AND ca.account_id = $1)
    WHERE
      c.slug = $2
    LIMIT 1
    """,
  ) {
    bindAccountId(0, accountId)
    bindComputerSlug(1, slug)
  }

  return rowIterator.singleOrNull {
    val computer = getComputer()

    val computerAccess = getComputerAccessIdOrNull(4)?.let { computerAccessId ->
      DbComputerAccess(
        id = computerAccessId,
        createdAt = getInstant(5)!!,
        version = getS64(6)!!,
        computerId = getComputerId(7),
        accountId = getAccountId(8),
        userId = getUserId(9),
      )
    }

    computer to computerAccess
  }
}

context(connection: SqlConnection)
suspend fun selectComputer(
  slug: ComputerSlug,
): DbComputer? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      $computerColumns
    FROM Computer c
    WHERE c.slug = $1
    LIMIT 1
    """,
  ) {
    bindComputerSlug(0, slug)
  }

  return rowIterator.singleOrNull {
    getComputer()
  }
}

private const val computerColumns =
  """
  c.id,
  c.created_at,
  c.version,
  c.slug
  """

private const val computerAccessColumns =
  """
  ca.id,
  ca.created_at,
  ca.version,
  ca.computer_id,
  ca.account_id,
  ca.user_id
  """

private fun SqlRow.getComputer() = DbComputer(
  id = getComputerId(0),
  createdAt = getInstant(1)!!,
  version = getS64(2)!!,
  slug = getComputerSlug(3),
)
