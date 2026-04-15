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
suspend fun findInvitesByClaimedBy(claimed_by: AccountId?, limit: Long): Invite? {
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
    WHERE claimed_by ${if (claimed_by == null) "IS" else "="} $1
    LIMIT $2
    """,
  ) {
    bindAccountId(0, claimed_by)
    bindS64(1, limit)
  }
  return rowIterator.singleOrNull { cursor ->
    Invite(
      cursor.getInviteId(0),
      cursor.getInstant(1)!!,
      cursor.getAccountId(2),
      cursor.getS32(3)!!,
      cursor.getString(4)!!,
      cursor.getInstant(5),
      cursor.getAccountIdOrNull(6),
    )
  }
}

context(connection: SqlConnection)
suspend fun findInvitesByCode(code: String): Invite? {
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
    WHERE code = $1
    """,
  ) {
    bindString(0, code)
  }

  return rowIterator.singleOrNull { cursor ->
    Invite(
      cursor.getInviteId(0),
      cursor.getInstant(1)!!,
      cursor.getAccountId(2),
      cursor.getS32(3)!!,
      cursor.getString(4)!!,
      cursor.getInstant(5),
      cursor.getAccountIdOrNull(6),
    )
  }
}

context(connection: SqlConnection)
suspend fun insertInvite(
  created_at: Instant,
  created_by: AccountId,
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
    bindInstant(0, created_at)
    bindAccountId(1, created_by)
    bindS32(2, version)
    bindString(3, code)
  }
}

context(connection: SqlConnection)
suspend fun claimInvite(
  new_version: Int,
  claimed_at: Instant?,
  claimed_by: AccountId?,
  expected_version: Int,
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
    bindS32(0, new_version)
    bindInstant(1, claimed_at)
    bindAccountId(2, claimed_by)
    bindS32(3, expected_version)
    bindInviteId(4, id)
  }
}
