package com.wasmo.computers

import com.wasmo.app.db2.WasmoDbTransaction as TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug

interface ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun initializeFromSpec(computerSpecToken: String): ComputerService

  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, slug: ComputerSlug): ComputerService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(computerId: ComputerId): ComputerService
}
