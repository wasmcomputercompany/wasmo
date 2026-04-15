package com.wasmo.db.computers

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindComputerId
import com.wasmo.db.getComputerAccessId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import com.wasmo.sql.single
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun insertComputerAccess(
  created_at: Instant,
  version: Int,
  computer_id: ComputerId,
  account_id: AccountId,
): ComputerAccessId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO ComputerAccess(
      created_at,
      version,
      computer_id,
      account_id
    )
    VALUES (
      $1,
      $2,
      $3,
      $4
    ) RETURNING id
    """,
  ) {
    bindInstant(0, created_at)
    bindS32(1, version)
    bindComputerId(2, computer_id)
    bindAccountId(3, account_id)
  }

  return rowIterator.single { cursor ->
    cursor.getComputerAccessId(0)
  }
}
