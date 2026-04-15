package com.wasmo.sql

import wasmo.sql.RowIterator
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlRow

suspend fun <T> SqlDatabase.transaction(
  block: suspend context(SqlTransaction) () -> T,
): T {
  val transaction = RealSqlTransaction(newConnection())
  transaction.use { transaction ->
    context(transaction) {
      val result = block()
      for (action in transaction.afterCommitActions) {
        action()
      }
      return result
    }
  }
}

suspend fun <T> SqlDatabase.withConnection(block: suspend context(SqlConnection) () -> T): T {
  newConnection().use { connection ->
    context(connection) {
      return block()
    }
  }
}

interface SqlTransaction : SqlConnection {
  fun afterCommit(function: () -> Unit)
}

internal class RealSqlTransaction(
  sqlConnection: SqlConnection,
) : SqlConnection by sqlConnection, SqlTransaction {
  val afterCommitActions = mutableListOf<() -> Unit>()

  override fun afterCommit(function: () -> Unit) {
    afterCommitActions += function
  }
}

suspend fun <T> RowIterator.list(mapper: (SqlRow) -> T): List<T> {
  use {
    return buildList {
      while (true) {
        val row = next() ?: break
        add(mapper(row))
      }
    }
  }
}

suspend fun <T> RowIterator.single(mapper: (SqlRow) -> T): T {
  use {
    val row = next() ?: error("expected one element but was none")
    val result = mapper(row)
    check(next() == null) { "expected one element but was multiple " }
    return result
  }
}

suspend fun <T> RowIterator.singleOrNull(mapper: (SqlRow) -> T): T? {
  use {
    val row = next() ?: return null
    val result = mapper(row)
    check(next() == null) { "expected at most one element but was multiple " }
    return result
  }
}
