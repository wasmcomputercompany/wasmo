package com.wasmo.db.sqlservice

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor

abstract class Query2<out RowType : Any>(
  val mapper: (SqlCursor) -> RowType,
) {
  abstract fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R>

  fun executeAsList(): List<RowType> = execute { cursor ->
    val result = mutableListOf<RowType>()
    while (cursor.next().value) result.add(mapper(cursor))
    QueryResult.Value(result)
  }.value

  fun executeAsOne(): RowType {
    return executeAsOneOrNull()
      ?: throw NullPointerException("ResultSet returned null for $this")
  }

  fun executeAsOneOrNull(): RowType? = execute { cursor ->
    if (!cursor.next().value) return@execute QueryResult.Value(null)
    val value = mapper(cursor)
    check(!cursor.next().value) { "ResultSet returned more than 1 row for $this" }
    QueryResult.Value(value)
  }.value
}
