package com.wasmo.app.db

import com.wasmo.app.db2.WasmoDbConnection
import com.wasmo.app.db2.WasmoDbTransaction
import okio.Closeable

abstract class WasmoDb : Closeable {
  abstract suspend fun newConnection(): WasmoDbConnection

  abstract suspend fun newTransaction(): WasmoDbTransaction

  suspend fun <T> transactionWithResult(
    noEnclosing: Boolean,
    block: suspend context(WasmoDbTransaction) () -> T,
  ): T {
    newTransaction().use { transaction ->
      context(transaction) {
        return block()
      }
    }
  }

  suspend fun <T> transaction(
    noEnclosing: Boolean,
    block: suspend context(WasmoDbTransaction) () -> T,
  ): T {
    newTransaction().use { transaction ->
      context(transaction) {
        return block()
      }
    }
  }

  suspend fun <T> withConnection(block: suspend context(WasmoDbConnection) () -> T): T {
    newConnection().use { connection ->
      context(connection) {
        return block()
      }
    }
  }
}
