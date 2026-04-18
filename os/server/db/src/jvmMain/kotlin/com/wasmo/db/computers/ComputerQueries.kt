package com.wasmo.db.computers

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindComputerId
import com.wasmo.db.bindComputerSlug
import com.wasmo.db.getComputerId
import com.wasmo.db.getComputerSlug
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.sql.list
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow

context(connection: SqlConnection)
suspend fun insertComputer(
  created_at: Instant,
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
    bindInstant(0, created_at)
    bindS64(1, version)
    bindComputerSlug(2, slug)
  }

  return rowIterator.single {
    getComputerId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectComputersByAccountId(
  account_id: AccountId,
  limit: Long,
): List<Computer> {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      c.id, c.created_at, c.version, c.slug
    FROM
      ComputerAccess ca,
      Computer c
    WHERE
      c.id = ca.id AND
      ca.account_id = $1
    ORDER BY
      c.slug
    LIMIT $2
    """,
  ) {
    bindAccountId(0, account_id)
    bindS64(1, limit)
  }

  return rowIterator.list {
    getComputer()
  }
}

context(connection: SqlConnection)
suspend fun selectComputerByAccountIdAndSlug(
  account_id: AccountId,
  slug: ComputerSlug,
): Computer? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      c.id, c.created_at, c.version, c.slug
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
    bindAccountId(0, account_id)
    bindComputerSlug(1, slug)
  }

  return rowIterator.singleOrNull {
    getComputer()
  }
}

context(connection: SqlConnection)
suspend fun selectComputerById(
  id: ComputerId,
): Computer {
  val rowIterator = connection.executeQuery(
    """
    SELECT Computer.id, Computer.created_at, Computer.version, Computer.slug
    FROM
      Computer
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

private fun SqlRow.getComputer() = Computer(
  id = getComputerId(0),
  created_at = getInstant(1)!!,
  version = getS64(2)!!,
  slug = getComputerSlug(3),
)
