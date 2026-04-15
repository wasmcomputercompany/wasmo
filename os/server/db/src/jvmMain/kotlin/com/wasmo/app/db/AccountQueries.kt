package com.wasmo.app.db

import com.wasmo.app.db2.WasmoDbConnection as SqlDriver
import com.wasmo.app.db2.getAccountId
import com.wasmo.app.db2.single
import com.wasmo.identifiers.AccountId

class AccountQueries(
  private val driver: SqlDriver,
) {
  suspend fun insertAccount(version: Int): AccountId {
    val rowIterator = driver.executeQuery(
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
}
