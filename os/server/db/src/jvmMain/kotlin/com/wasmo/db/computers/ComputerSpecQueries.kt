package com.wasmo.db.computers

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindComputerId
import com.wasmo.db.bindComputerSlug
import com.wasmo.db.bindComputerSpecId
import com.wasmo.db.getAccountId
import com.wasmo.db.getComputerIdOrNull
import com.wasmo.db.getComputerSlug
import com.wasmo.db.getComputerSpecId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun selectComputerSpecByToken(token: String): DbComputerSpec? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      ComputerSpec.id,
      ComputerSpec.created_at,
      ComputerSpec.version,
      ComputerSpec.account_id,
      ComputerSpec.token,
      ComputerSpec.slug,
      ComputerSpec.computer_id
    FROM ComputerSpec
    WHERE token = $1
    LIMIT 1
    """,
  ) {
    bindString(0, token)
  }

  return rowIterator.singleOrNull {
    DbComputerSpec(
      getComputerSpecId(0),
      getInstant(1)!!,
      getS64(2)!!,
      getAccountId(3),
      getString(4)!!,
      getComputerSlug(5),
      getComputerIdOrNull(6),
    )
  }
}

context(connection: SqlConnection)
suspend fun insertComputerSpec(
  created_at: Instant,
  version: Long,
  account_id: AccountId,
  token: String,
  slug: ComputerSlug,
): ComputerSpecId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO ComputerSpec(
      created_at,
      version,
      account_id,
      token,
      slug
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
    bindInstant(0, created_at)
    bindS64(1, version)
    bindAccountId(2, account_id)
    bindString(3, token)
    bindComputerSlug(4, slug)
  }
  return rowIterator.single {
    getComputerSpecId(0)
  }
}

context(connection: SqlConnection)
suspend fun linkComputer(
  new_version: Long,
  computer_id: ComputerId?,
  expected_version: Long,
  id: ComputerSpecId,
): Long {
  return connection.execute(
    """
    UPDATE ComputerSpec
    SET
      version = $1,
      computer_id = $2
    WHERE
      version = $3 AND
      id = $4
    """,
  ) {
    bindS64(0, new_version)
    bindComputerId(1, computer_id)
    bindS64(2, expected_version)
    bindComputerSpecId(3, id)
  }
}
