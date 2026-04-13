package com.wasmo.sqldelight

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.r2dbc.R2dbcCursor
import app.cash.sqldelight.driver.r2dbc.R2dbcPreparedStatement
import okio.ByteString.Companion.toByteString
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlRow

fun SqlDatabase.driver(): SqlDriver = WasmoSqlDriver(this)

/**
 * Implement SQLDelight's runtime API on Wasmo's runtime API.
 *
 * This is incomplete.
 */
private class WasmoSqlDriver(
  private val database: SqlDatabase,
) : SqlDriver {
  override fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: (SqlCursor) -> QueryResult<R>,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<R> {
    return QueryResult.AsyncValue {
      database.newConnection().use { connection ->
        connection.executeQuery(
          sql = sql,
          bindParameters = {
            if (binders != null) {
              WasmoPreparedStatement(this).binders()
            }
          },
        ).use { rowIterator ->
          mapper(WasmoSqlCursor(rowIterator)).await()
        }
      }
    }
  }

  override fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<Long> {
    return QueryResult.AsyncValue {
      database.newConnection().use { connection ->
        connection.execute(
          sql = sql,
          bindParameters = {
            if (binders != null) {
              WasmoPreparedStatement(this).binders()
            }
          },
        )
      }
    }
  }

  override fun newTransaction(): QueryResult<Transacter.Transaction> {
    error("TODO")
  }

  override fun currentTransaction(): Transacter.Transaction? {
    return object : Transacter.Transaction() {
      override val enclosingTransaction: Transacter.Transaction?
        get() = null

      override fun endTransaction(successful: Boolean): QueryResult<Unit> {
        return QueryResult.AsyncValue { Unit }
      }
    }
  }

  override fun addListener(
    vararg queryKeys: String,
    listener: Query.Listener,
  ) {
  }

  override fun notifyListeners(vararg queryKeys: String) {
  }

  override fun removeListener(
    vararg queryKeys: String,
    listener: Query.Listener,
  ) {
  }

  override fun close() {
    error("TODO")
  }
}

private class WasmoPreparedStatement(
  private val sqlBinder: SqlBinder,
) : SqlPreparedStatement, R2dbcPreparedStatement {
  override fun bindBoolean(index: Int, boolean: Boolean?) {
    sqlBinder.bindBool(index, boolean)
  }

  override fun bindBytes(index: Int, bytes: ByteArray?) {
    sqlBinder.bindBytes(index, bytes?.toByteString())
  }

  override fun bindDouble(index: Int, double: Double?) {
    sqlBinder.bindF64(index, double)
  }

  override fun bindLong(index: Int, long: Long?) {
    sqlBinder.bindS64(index, long)
  }

  override fun bindString(index: Int, string: String?) {
    sqlBinder.bindString(index, string)
  }
}

private class WasmoSqlCursor(
  private val rowIterator: RowIterator,
) : SqlCursor, R2dbcCursor {
  private var row: SqlRow? = null

  override fun next() = QueryResult.AsyncValue {
    val row = rowIterator.next()
    this@WasmoSqlCursor.row = row
    row != null
  }

  override fun getBoolean(index: Int) = row!!.getBool(index)

  override fun getBytes(index: Int) = row!!.getBytes(index)?.toByteArray()

  override fun getDouble(index: Int) = row!!.getF64(index)

  override fun getLong(index: Int): Long? = row!!.getS64(index)

  override fun getString(index: Int) = row!!.getString(index)
}
