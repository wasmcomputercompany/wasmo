package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug

interface ComputerStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun initializeFromSpec(computerSpecToken: String): WasmoComputer

  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, slug: ComputerSlug): WasmoComputer?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(computerId: ComputerId): WasmoComputer
}
