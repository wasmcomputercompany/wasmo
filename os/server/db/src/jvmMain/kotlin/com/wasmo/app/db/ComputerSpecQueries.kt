package com.wasmo.app.db

import com.wasmo.identifiers.AccountId
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSpecId
import com.wasmo.sql.single
import com.wasmo.sql.singleOrNull
import kotlin.time.Instant
import wasmo.sql.SqlConnection

suspend fun SqlConnection.selectComputerSpecByToken(token: String): ComputerSpec? {
  val rowIterator = executeQuery(
    """
    SELECT ComputerSpec.id, ComputerSpec.created_at, ComputerSpec.version, ComputerSpec.account_id, ComputerSpec.token, ComputerSpec.slug, ComputerSpec.computer_id
    FROM
      ComputerSpec
    WHERE
      token = $1
    LIMIT 1
    """,
  ) {
    var parameterIndex = 0
    bindString(parameterIndex++, token)
  }

  return rowIterator.singleOrNull { cursor ->
    ComputerSpec(
      cursor.getComputerSpecId(0),
      cursor.getInstant(1)!!,
      cursor.getS64(2)!!,
      cursor.getAccountId(3),
      cursor.getString(4)!!,
      cursor.getComputerSlug(5),
      cursor.getComputerIdOrNull(6),
    )
  }
}

suspend fun SqlConnection.insertComputerSpec(
  created_at: Instant,
  version: Long,
  account_id: AccountId,
  token: String,
  slug: ComputerSlug,
): ComputerSpecId {
  val rowIterator = executeQuery(
    """
    INSERT INTO ComputerSpec(
      created_at,
      version,
      account_id,
      token,
      slug
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5
    ) RETURNING id
    """,
  ) {
    var parameterIndex = 0
    bindInstant(parameterIndex++, created_at)
    bindS64(parameterIndex++, version)
    bindAccountId(parameterIndex++, account_id)
    bindString(parameterIndex++, token)
    bindComputerSlug(parameterIndex++, slug)
  }
  return rowIterator.single { cursor ->
    cursor.getComputerSpecId(0)
  }
}

suspend fun SqlConnection.linkComputer(
  new_version: Long,
  computer_id: ComputerId?,
  expected_version: Long,
  id: ComputerSpecId,
): Long {
  return execute(
    """
    UPDATE ComputerSpec
    SET
      version = $1,
      computer_id = $2
    WHERE
      version = $3 AND
      id = $4
    """,
  ) {
    var parameterIndex = 0
    bindS64(parameterIndex++, new_version)
    bindComputerId(parameterIndex++, computer_id)
    bindS64(parameterIndex++, expected_version)
    bindComputerSpecId(parameterIndex++, id)
  }
}
