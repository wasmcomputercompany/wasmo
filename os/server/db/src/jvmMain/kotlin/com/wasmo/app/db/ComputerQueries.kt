package com.wasmo.app.db

import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.bindAccountId
import com.wasmo.app.db2.bindComputerId
import com.wasmo.app.db2.bindComputerSlug
import com.wasmo.app.db2.getComputerId
import com.wasmo.app.db2.getComputerSlug
import com.wasmo.app.db2.list
import com.wasmo.app.db2.single
import com.wasmo.app.db2.singleOrNull
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import kotlin.time.Instant

class ComputerQueries(
  private val driver: SqlDriver,
) {
  suspend fun insertComputer(
    created_at: Instant,
    version: Long,
    slug: ComputerSlug,
  ): ComputerId {
    val rowIterator = driver.executeQuery(
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
      var parameterIndex = 0
      bindInstant(parameterIndex++, created_at)
      bindS64(parameterIndex++, version)
      bindComputerSlug(parameterIndex++, slug)
    }

    return rowIterator.single { cursor ->
      cursor.getComputerId(0)
    }
  }

  suspend fun selectComputersByAccountId(account_id: AccountId, limit: Long): List<Computer> {
    val rowIterator = driver.executeQuery(
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
      var parameterIndex = 0
      bindAccountId(parameterIndex++, account_id)
      bindS64(parameterIndex++, limit)
    }

    return rowIterator.list { cursor ->
      Computer(
        cursor.getComputerId(0),
        cursor.getInstant(1)!!,
        cursor.getS64(2)!!,
        cursor.getComputerSlug(3),
      )
    }
  }

  suspend fun selectComputerByAccountIdAndSlug(
    account_id: AccountId,
    slug: ComputerSlug,
  ): Computer? {
    val rowIterator = driver.executeQuery(
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
      var parameterIndex = 0
      bindAccountId(parameterIndex++, account_id)
      bindComputerSlug(parameterIndex++, slug)
    }

    return rowIterator.singleOrNull { cursor ->
      Computer(
        cursor.getComputerId(0),
        cursor.getInstant(1)!!,
        cursor.getS64(2)!!,
        cursor.getComputerSlug(3),
      )
    }
  }

  suspend fun selectComputerById(
    id: ComputerId,
  ): Computer {
    val rowIterator = driver.executeQuery(
      """
      SELECT Computer.id, Computer.created_at, Computer.version, Computer.slug
      FROM
        Computer
      WHERE
        id = $1
      LIMIT 1
      """,
    ) {
      var parameterIndex = 0
      bindComputerId(parameterIndex++, id)
    }

    return rowIterator.single { cursor ->
      Computer(
        cursor.getComputerId(0),
        cursor.getInstant(1)!!,
        cursor.getS64(2)!!,
        cursor.getComputerSlug(3),
      )
    }
  }
}
