package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.api.ComputerSlug
import com.wasmo.identifiers.ComputerId

interface ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun initializeFromSpec(computerSpecToken: String): WasmoComputer

  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, slug: ComputerSlug): WasmoComputer?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(computerId: ComputerId): WasmoComputer
}
