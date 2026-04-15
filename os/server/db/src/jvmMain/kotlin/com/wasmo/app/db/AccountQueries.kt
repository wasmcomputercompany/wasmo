package com.wasmo.app.db

import com.wasmo.identifiers.AccountId

suspend fun WasmoDbConnection.insertAccount(version: Int): AccountId {
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
