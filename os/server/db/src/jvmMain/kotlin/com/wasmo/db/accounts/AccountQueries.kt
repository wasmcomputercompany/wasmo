package com.wasmo.db.accounts

import com.wasmo.db.emails.DbLinkedEmailAddress
import com.wasmo.db.getAccountId
import com.wasmo.db.getLinkedEmailAddressId
import com.wasmo.identifiers.AccountId
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.list
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

context(connection: SqlConnection)
suspend fun listAccounts(limit: Int = Int.MAX_VALUE): List<AccountId> =
  connection.executeQuery(
    sql = """
      SELECT * FROM Account LIMIT $1
    """,
  ) {
    bindS32(0, limit)
  }.list {
    getAccountId(0)
  }
