package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerAccessId
import com.wasmo.identifiers.ComputerId
import kotlin.time.Instant

suspend fun WasmoDbConnection.insertComputerAccess(
  created_at: Instant,
  version: Int,
  computer_id: ComputerId,
  account_id: AccountId,
): ComputerAccessId {
  val rowIterator = executeQuery(
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
    var parameterIndex = 0
    bindInstant(parameterIndex++, created_at)
    bindS32(parameterIndex++, version)
    bindComputerId(parameterIndex++, computer_id)
    bindAccountId(parameterIndex++, account_id)
  }

  return rowIterator.single { cursor ->
    cursor.getComputerAccessId(0)
  }
}
