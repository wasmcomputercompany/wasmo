package com.wasmo.db.sqlservice

import com.wasmo.app.db2.RealSqlCursor
import com.wasmo.app.db2.RealSqlCursor as SqlCursor
import wasmo.sql.RowIterator

abstract class Query2<out RowType : Any>(
  val mapper: suspend (SqlCursor) -> RowType,
) {
  abstract suspend fun execute(): RowIterator

  suspend fun <R> execute(mapper: suspend (SqlCursor) -> R): R {
    val rowIterator = execute()
    return mapper(RealSqlCursor(rowIterator))
  }

  suspend fun executeAsList(): List<RowType> = execute { cursor ->
    val result = mutableListOf<RowType>()
    while (cursor.next()) result.add(mapper(cursor))
    result
  }

  suspend fun executeAsOne(): RowType {
    return executeAsOneOrNull()
      ?: throw NullPointerException("ResultSet returned null for $this")
  }

  suspend fun executeAsOneOrNull(): RowType? = execute { cursor ->
    if (!cursor.next()) return@execute null
    val value = mapper(cursor)
    check(!cursor.next()) { "ResultSet returned more than 1 row for $this" }
    value
  }
}
