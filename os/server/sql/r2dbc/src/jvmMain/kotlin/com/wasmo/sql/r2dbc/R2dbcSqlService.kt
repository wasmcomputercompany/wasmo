package com.wasmo.sql.r2dbc

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.postgresql.api.PostgresqlStatement
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitSingle
import okio.ByteString
import okio.ByteString.Companion.toByteString
import reactor.core.publisher.Flux
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlRow
import wasmo.sql.SqlService

fun PostgresqlConnectionFactory.asSqlService(): SqlService = R2dbcSqlService(this)

/**
 * Adapt Wasmo's coroutines SQL API to R2DBC's reactive streams implementation.
 *
 * This is similar to SQLDelight's `R2dbcDriver`, though our interfaces make slightly different
 * design tradeoffs.
 *
 * https://github.com/sqldelight/sqldelight/blob/7981477e63f3c58df97f87613c372ef6945e8924/drivers/r2dbc-driver/src/main/kotlin/app/cash/sqldelight/driver/r2dbc/R2dbcDriver.kt#L24
 */
private class R2dbcSqlService(
  private val connectionFactory: PostgresqlConnectionFactory,
) : SqlService {
  override suspend fun getOrCreate(name: String): SqlDatabase {
    require(name == "") {
      "SqlService doesn't support named databases yet"
    }

    return R2dbcSqlDatabase(connectionFactory)
  }
}

private class R2dbcSqlDatabase(
  private val connectionFactory: PostgresqlConnectionFactory,
) : SqlDatabase {
  override suspend fun newConnection(): SqlConnection =
    R2dbcSqlConnection(connectionFactory.create().awaitSingle())

  override fun close() {
    // TODO: close all connections
  }
}

private class R2dbcSqlBinder(
  private val statement: PostgresqlStatement,
) : SqlBinder {
  override fun bindString(index: Int, value: String?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, String::class.java)
    }
  }

  override fun bindLong(index: Int, value: Long?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Long::class.javaObjectType)
    }
  }

  override fun bindBytes(index: Int, value: ByteString?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, ByteArray::class.java)
    }
  }

  override fun bindDouble(index: Int, value: Double?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Double::class.javaObjectType)
    }
  }

  override fun bindBoolean(index: Int, value: Boolean?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Boolean::class.javaObjectType)
    }
  }
}

private class R2dbcSqlConnection(
  private val connection: PostgresqlConnection,
) : SqlConnection {
  override suspend fun execute(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ): Long {
    val statement = connection.createStatement(sql)
    if (bindParameters != null) {
      R2dbcSqlBinder(statement).bindParameters()
    }
    val result = statement.execute().awaitSingle()!!
    return result.rowsUpdated.awaitFirstOrDefault(0L)
  }

  override suspend fun executeQuery(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ): RowIterator {
    val statement = connection.createStatement(sql)
    if (bindParameters != null) {
      R2dbcSqlBinder(statement).bindParameters()
    }

    return statement
      .execute()
      .awaitSingle()
      .map { row, rowMetadata ->
        R2dbcSqlRow(
          Array(rowMetadata.columnMetadatas.size) { index ->
            row.get(index)
          },
        )
      }
      .asRowIterator()
  }

  override fun close() {
    connection.close()
  }
}

private fun Flux<out SqlRow>.asRowIterator() = object : RowIterator {
  private val delegate = PublisherIterator(this@asRowIterator)

  override suspend fun next() = delegate.next()

  override fun close() {
    delegate.close()
  }
}

private class R2dbcSqlRow(
  private val values: Array<Any?>,
) : SqlRow {
  override fun getString(columnIndex: Int) = values[columnIndex] as String?

  override fun getLong(columnIndex: Int) = (values[columnIndex] as Number?)?.toLong()

  override fun getBytes(columnIndex: Int) = (values[columnIndex] as ByteArray?)?.toByteString()

  override fun getDouble(columnIndex: Int) = (values[columnIndex] as Number?)?.toDouble()

  override fun getBoolean(columnIndex: Int) = values[columnIndex] as Boolean?
}
