package com.wasmo.calls

import com.wasmo.sql.SqlTransaction

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
}
