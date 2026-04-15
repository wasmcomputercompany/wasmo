package com.wasmo.db.accounts

import com.wasmo.db.getAccountId
import com.wasmo.identifiers.AccountId
import com.wasmo.sql.single
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun insertAccount(version: Int): AccountId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO Account(
      version
    )
    VALUES (
      $1
    ) RETURNING id
    """,
  ) {
    var parameterIndex = 0
    bindS32(parameterIndex++, version)
  }

  return rowIterator.single { cursor ->
    cursor.getAccountId(0)
  }
}
