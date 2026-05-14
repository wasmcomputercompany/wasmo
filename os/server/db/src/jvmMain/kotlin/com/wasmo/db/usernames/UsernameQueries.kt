package com.wasmo.db.usernames

import com.wasmo.db.bindAccountId
import com.wasmo.db.getAccountId
import com.wasmo.db.getUsernameId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.UsernameSlug
import kotlin.time.Instant
import wasmo.sql.SqlConnection
import wasmo.sql.SqlException
import wasmo.sql.SqlRow
import wasmox.sql.SqlTransaction
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

/**
 * Deletes [replacedUsername] at [replacedAt] and inserts a new
 * [replacementUsername] row for the same [accountId], created at [replacedAt].
 *
 * @throws SqlException if [replacedUsername] is not found, doesn't belong to the given
 *      account, or is already deleted.
 */
context(sqlTransaction: SqlTransaction)
suspend fun replaceUsername(
  replacedAt: Instant,
  accountId: AccountId,
  replacedUsername: UsernameSlug,
  replacementUsername: UsernameSlug,
): Long {
  val rowsAffectedByDelete = deleteUsername(
    deletedAt = replacedAt,
    accountId = accountId,
    username = replacedUsername,
  )
  if (rowsAffectedByDelete == 0L) {
    throw SqlException("Failed to delete username $replacedUsername for accountId $accountId")
  }
  val rowsAffectedByInsert = insertUsername(
    createdAt = replacedAt,
    accountId = accountId,
    usernameSlug = replacementUsername,
  )
  return rowsAffectedByInsert + rowsAffectedByDelete
}

/**
 * Marks [username] for the given [accountId] as deleted at [deletedAt].
 * A deleted username will no longer be found by [selectUsernameOrNull].
 * Deletion is meant to be permanent, but we might some dayy want to offer
 * un-deletion/reuse to the account originally owning the username.
 */
context(connection: SqlConnection)
private suspend fun deleteUsername(
  deletedAt: Instant,
  accountId: AccountId,
  username: UsernameSlug,
): Long =
  connection.execute(
    """
      UPDATE Username
      SET deleted_at = $1
      WHERE account_id = $2 AND username = $3 AND deleted_at IS NULL
    """.trimIndent()
  ) {
    bindInstant(0, deletedAt)
    bindAccountId(1, accountId)
    bindString(2, username.value)
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
      username,
      deleted_at
    FROM Username
    WHERE account_id = $1 AND deleted_at IS NULL
    ORDER BY created_at ASC
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
  deletedAt = getInstant(4)
)
