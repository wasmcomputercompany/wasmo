package com.wasmo.calls

import app.cash.sqldelight.TransactionCallbacks

internal abstract class DbLazy<T> {
  var loaded = false
  var cached: T? = null

  context(transactionCallbacks: TransactionCallbacks)
  fun get(): T {
    if (loaded) return cached as T

    return load()
      .also { cached = it }
  }

  context(transactionCallbacks: TransactionCallbacks)
  protected abstract fun load(): T
}
