package com.wasmo.sql.jdbc

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.apache.commons.dbcp2.PoolingDataSource
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlRow
import wasmo.sql.SqlService

fun PoolingDataSource<*>.asSqlService(): SqlService = JdbcSqlService(this)

private class JdbcSqlService(
  private val dataSource: PoolingDataSource<*>,
) : SqlService {
  override suspend fun getOrCreate(name: String?): SqlDatabase {
    require(name == null) {
      "SqlService doesn't support named databases yet"
    }

    return JdbcSqlDatabase(dataSource)
  }
}

private class JdbcSqlDatabase(
  private val dataSource: PoolingDataSource<*>,
) : SqlDatabase {
  override suspend fun newConnection(): SqlConnection =
    JdbcSqlConnection(dataSource.connection)

  override fun close() {
    dataSource.close()
  }
}

private class JdbcSqlBinder(
  private val statement: PreparedStatement,
) : SqlBinder {
  override fun bindString(index: Int, value: String?) {
    statement.setString(index + 1, value)
  }

  override fun bindLong(index: Int, value: Long?) {
    when {
      value != null -> statement.setLong(index + 1, value)
      else -> statement.setNull(index + 1, Types.BIGINT)
    }
  }

  override fun bindBytes(index: Int, value: ByteString?) {
    statement.setBytes(index + 1, value?.toByteArray())
  }

  override fun bindDouble(index: Int, value: Double?) {
    when {
      value != null -> statement.setDouble(index + 1, value)
      else -> statement.setNull(index + 1, Types.DOUBLE)
    }
  }

  override fun bindBoolean(index: Int, value: Boolean?) {
    when {
      value != null -> statement.setBoolean(index + 1, value)
      else -> statement.setNull(index + 1, Types.BOOLEAN)
    }
  }
}

private class JdbcSqlConnection(
  private val connection: Connection,
) : SqlConnection {
  override suspend fun execute(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ): Long {
    val statement = connection.prepareStatement(sql)
    if (bindParameters != null) {
      JdbcSqlBinder(statement).bindParameters()
    }
    return statement.executeUpdate().toLong()
  }

  override suspend fun executeQuery(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ): RowIterator {
    val statement = connection.prepareStatement(sql)
    if (bindParameters != null) {
      JdbcSqlBinder(statement).bindParameters()
    }
    return JdbcRowIterator(statement.executeQuery())
  }

  override fun close() {
    connection.close()
  }
}

private class JdbcRowIterator(
  private val resultSet: ResultSet,
) : RowIterator {
  private var lastRow: JdbcSqlRow? = null

  override suspend fun next(): SqlRow? {
    lastRow?.closed = true
    return when {
      resultSet.next() -> JdbcSqlRow(resultSet).also { lastRow = it }
      else -> null
    }
  }

  override fun close() {
    lastRow?.closed = true
    lastRow = null
    resultSet.close()
  }
}

private class JdbcSqlRow(
  private val resultSet: ResultSet,
) : SqlRow {
  var closed = false

  override fun getString(columnIndex: Int): String? {
    check(!closed) { "cannot getString() after next() or close()" }
    return resultSet.getString(columnIndex + 1)
  }

  override fun getLong(columnIndex: Int): Long? {
    check(!closed) { "cannot getLong() after next() or close()" }
    val longValue = resultSet.getLong(columnIndex + 1)
    return when {
      resultSet.wasNull() -> null
      else -> longValue
    }
  }

  override fun getBytes(columnIndex: Int): ByteString? {
    check(!closed) { "cannot getBytes() after next() or close()" }
    return resultSet.getBytes(columnIndex + 1)?.toByteString()
  }

  override fun getDouble(columnIndex: Int): Double? {
    check(!closed) { "cannot getDouble() after next() or close()" }
    val longValue = resultSet.getDouble(columnIndex + 1)
    return when {
      resultSet.wasNull() -> null
      else -> longValue
    }
  }

  override fun getBoolean(columnIndex: Int): Boolean? {
    check(!closed) { "cannot getBoolean() after next() or close()" }
    val longValue = resultSet.getBoolean(columnIndex + 1)
    return when {
      resultSet.wasNull() -> null
      else -> longValue
    }
  }
}
