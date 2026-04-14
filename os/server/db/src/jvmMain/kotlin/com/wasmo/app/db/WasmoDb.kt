package com.wasmo.app.db

import com.wasmo.app.db2.WasmoDbConnection
import com.wasmo.app.db2.WasmoDbTransaction
import okio.Closeable

interface WasmoDb : Closeable {
  suspend fun <T> transactionWithResult(
    noEnclosing: Boolean,
    block: suspend context(WasmoDbTransaction) () -> T,
  ): T

  suspend fun <T> transaction(
    noEnclosing: Boolean,
    block: suspend context(WasmoDbTransaction) () -> T,
  ): T

  suspend fun <T> withConnection(block: suspend context(WasmoDbConnection) () -> T): T
}
