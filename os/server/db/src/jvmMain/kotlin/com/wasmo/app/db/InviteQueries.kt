package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.InviteId
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

suspend fun SqlConnection.findInvitesByClaimedBy(claimed_by: AccountId?, limit: Long): Invite? {
  val rowIterator = executeQuery(
    """SELECT Invite.id, Invite.created_at, Invite.created_by, Invite.version, Invite.code, Invite.claimed_at, Invite.claimed_by FROM Invite WHERE claimed_by ${if (claimed_by == null) "IS" else "="} $1 LIMIT $2""",
  ) {
    var parameterIndex = 0
    bindAccountId(parameterIndex++, claimed_by)
    bindS64(parameterIndex++, limit)
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

suspend fun SqlConnection.findInvitesByCode(code: String): Invite? {
  val rowIterator = executeQuery(
    """SELECT Invite.id, Invite.created_at, Invite.created_by, Invite.version, Invite.code, Invite.claimed_at, Invite.claimed_by FROM Invite WHERE code = $1""",
  ) {
    var parameterIndex = 0
    bindString(parameterIndex++, code)
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

suspend fun SqlConnection.insertInvite(
  created_at: Instant,
  created_by: AccountId,
  version: Int,
  code: String,
): Long {
  return execute(
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
    var parameterIndex = 0
    bindInstant(parameterIndex++, created_at)
    bindAccountId(parameterIndex++, created_by)
    bindS32(parameterIndex++, version)
    bindString(parameterIndex++, code)
  }
}

suspend fun SqlConnection.claimInvite(
  new_version: Int,
  claimed_at: Instant?,
  claimed_by: AccountId?,
  expected_version: Int,
  id: InviteId,
): Long {
  return execute(
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
    var parameterIndex = 0
    bindS32(parameterIndex++, new_version)
    bindInstant(parameterIndex++, claimed_at)
    bindAccountId(parameterIndex++, claimed_by)
    bindS32(parameterIndex++, expected_version)
    bindInviteId(parameterIndex++, id)
  }
}
