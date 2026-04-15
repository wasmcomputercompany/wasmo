package com.wasmo.accounts

import com.wasmo.sql.SqlTransaction

/** Used for actions that don't access user data. */
internal class UnauthenticatedCaller(
  override val userAgent: String?,
  override val ip: String?,
) : Caller {
  context(sqlTransaction: SqlTransaction)
  override suspend fun getAccountIdOrNull() = null
}
