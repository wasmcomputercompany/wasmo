package com.wasmo.db.accounts.invite

import com.wasmo.db.bindAccountId
import com.wasmo.db.bindInviteId
import com.wasmo.db.getAccountId
import com.wasmo.db.getAccountIdOrNull
import com.wasmo.db.getInviteId
import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

context(connection: SqlConnection)
suspend fun findInvitesByClaimedBy(
  claimedBy: AccountId?,
  limit: Long,
): DbInvite? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      Invite.id,
      Invite.created_at,
      Invite.created_by,
      Invite.version,
      Invite.code,
      Invite.claimed_at,
      Invite.claimed_by
    FROM Invite
    WHERE claimed_by ${if (claimedBy == null) "IS" else "="} $1
    LIMIT $2
    """,
  ) {
    bindAccountId(0, claimedBy)
    bindS64(1, limit)
  }
  return rowIterator.singleOrNull {
    DbInvite(
      getInviteId(0),
      getInstant(1)!!,
      getAccountId(2),
      getS32(3)!!,
      getString(4)!!,
      getInstant(5),
      getAccountIdOrNull(6),
    )
  }
}

context(connection: SqlConnection)
suspend fun findInvitesByCode(code: String): DbInvite? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      created_at,
      created_by,
      version,
      code,
      claimed_at,
      claimed_by
    FROM Invite
    WHERE code = $1
    """,
  ) {
    bindString(0, code)
  }

  return rowIterator.singleOrNull {
    DbInvite(
      getInviteId(0),
      getInstant(1)!!,
      getAccountId(2),
      getS32(3)!!,
      getString(4)!!,
      getInstant(5),
      getAccountIdOrNull(6),
    )
  }
}

context(connection: SqlConnection)
suspend fun insertInvite(
  createdAt: Instant,
  createdBy: AccountId,
  version: Int,
  code: String,
): Long {
  return connection.execute(
    """
    INSERT INTO Invite(
      created_at,
      created_by,
      version,
      code
    )
    VALUES (
      $1,
      $2,
      $3,
      $4
    )
    """,
  ) {
    bindInstant(0, createdAt)
    bindAccountId(1, createdBy)
    bindS32(2, version)
    bindString(3, code)
  }
}

context(connection: SqlConnection)
suspend fun claimInvite(
  newVersion: Int,
  claimedAt: Instant?,
  claimedBy: AccountId?,
  expectedVersion: Int,
  id: InviteId,
): Long {
  return connection.execute(
    """
    UPDATE Invite
    SET
      version = $1,
      claimed_at = $2,
      claimed_by = $3
    WHERE
      version = $4 AND
      id = $5
    """,
  ) {
    bindS32(0, newVersion)
    bindInstant(1, claimedAt)
    bindAccountId(2, claimedBy)
    bindS32(3, expectedVersion)
    bindInviteId(4, id)
  }
}
