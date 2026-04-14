package com.wasmo.db.sqlservice

import app.cash.sqldelight.db.SqlCursor

abstract class Query2<out RowType : Any>(
  val mapper: (SqlCursor) -> RowType,
) {
  abstract suspend fun <R> execute(mapper: (SqlCursor) -> R): R

  suspend fun executeAsList(): List<RowType> = execute { cursor ->
    val result = mutableListOf<RowType>()
    while (cursor.next().value) result.add(mapper(cursor))
    result
  }

  suspend fun executeAsOne(): RowType {
    return executeAsOneOrNull()
      ?: throw NullPointerException("ResultSet returned null for $this")
  }

  suspend fun executeAsOneOrNull(): RowType? = execute { cursor ->
    if (!cursor.next().value) return@execute null
    val value = mapper(cursor)
    check(!cursor.next().value) { "ResultSet returned more than 1 row for $this" }
    value
  }
}
