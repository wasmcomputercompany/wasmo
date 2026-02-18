package com.wasmo.accounts

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.AccountSnapshot

interface AccountStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): AccountSnapshot

  interface Factory {
    fun create(client: Client): AccountStore
  }
}
