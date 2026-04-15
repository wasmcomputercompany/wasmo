package com.wasmo.app.db

import okio.Closeable
import wasmo.sql.SqlConnection

interface WasmoDb : Closeable {
  suspend fun <T> transactionWithResult(
    noEnclosing: Boolean,
    block: suspend context(SqlTransaction) () -> T,
  ): T

  suspend fun <T> transaction(
    noEnclosing: Boolean,
    block: suspend context(SqlTransaction) () -> T,
  ): T

  suspend fun <T> withConnection(block: suspend context(SqlConnection) () -> T): T
}
