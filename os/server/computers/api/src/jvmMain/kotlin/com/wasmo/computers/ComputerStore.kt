package com.wasmo.computers

import com.wasmo.sql.SqlTransaction
import com.wasmo.accounts.Client
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug

interface ComputerStore {
  context(sqlTransaction: SqlTransaction)
  suspend fun initializeFromSpec(computerSpecToken: String): ComputerService

  context(sqlTransaction: SqlTransaction)
  suspend fun getOrNull(client: Client, slug: ComputerSlug): ComputerService?

  context(sqlTransaction: SqlTransaction)
  suspend fun get(computerId: ComputerId): ComputerService
}
