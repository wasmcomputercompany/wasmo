package com.wasmo.db.accounts

import com.wasmo.db.getAccountId
import com.wasmo.identifiers.AccountId
import wasmo.sql.SqlConnection
import wasmox.sql.single

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
    bindS32(0, version)
  }

  return rowIterator.single {
    getAccountId(0)
  }
}
