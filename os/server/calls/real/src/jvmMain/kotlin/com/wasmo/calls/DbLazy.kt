package com.wasmo.calls

import wasmox.sql.SqlTransaction

internal abstract class DbLazy<T> {
  var loaded = false
  var cached: T? = null

  context(sqlTransaction: SqlTransaction)
  suspend fun get(): T {
    if (loaded) return cached as T

    return load()
      .also { cached = it }
  }

  context(sqlTransaction: SqlTransaction)
  protected abstract suspend fun load(): T

  fun invalidate() {
    loaded = false
    cached = null
  }
}
