package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.sql.single
import wasmo.sql.SqlConnection

suspend fun SqlConnection.insertAccount(version: Int): AccountId {
  val rowIterator = executeQuery(
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
