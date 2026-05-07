package com.wasmo.db.usernames

import com.wasmo.db.bindAccountId
import com.wasmo.db.getAccountId
import com.wasmo.db.getUsernameId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.UsernameSlug
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.singleOrNull

context(connection: SqlConnection)
suspend fun insertUsername(
  createdAt: Instant,
  accountId: AccountId,
  usernameSlug: UsernameSlug,
): Long {
  return connection.execute(
    """
    INSERT INTO Username(
      created_at,
      account_id,
      username
    )
    VALUES (
      $1,
      $2,
      $3
    )
    """,
  ) {
    bindInstant(0, createdAt)
    bindAccountId(1, accountId)
    bindString(2, usernameSlug.value)
  }
}


context(connection: SqlConnection)
suspend fun selectUsernameOrNull(
  accountId: AccountId,
): DbUsername? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      account_id,
      username
    FROM Username
    WHERE account_id = $1
    ORDER BY created_at
    LIMIT $2
    """,
  ) {
    bindAccountId(0, accountId)
    bindS64(1, 1)
  }
  return rowIterator.singleOrNull {
    getUsername()
  }
}


private fun SqlRow.getUsername() = DbUsername(
  id = getUsernameId(0),
  createdAt = getInstant(1)!!,
  accountId = getAccountId(2),
  usernameSlug = UsernameSlug(getString(3)!!),
)
