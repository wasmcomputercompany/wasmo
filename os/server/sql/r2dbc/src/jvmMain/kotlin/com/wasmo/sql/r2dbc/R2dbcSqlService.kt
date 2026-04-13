@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.sql.r2dbc

import com.wasmo.support.closetracker.CloseListener
import com.wasmo.support.closetracker.CloseTracker
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Statement
import java.nio.ByteBuffer
import java.time.temporal.Temporal
import java.util.UUID
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitSingle
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.reactivestreams.Publisher
import wasmo.json.JsonLiteral
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlRow
import wasmo.sql.SqlService

fun ConnectionPool.asSqlService(): SqlService = R2dbcSqlService(this)

/**
 * Adapt Wasmo's coroutines SQL API to R2DBC's reactive streams implementation.
 *
 * This is similar to SQLDelight's `R2dbcDriver`, though our interfaces make slightly different
 * design tradeoffs.
 *
 * https://github.com/sqldelight/sqldelight/blob/7981477e63f3c58df97f87613c372ef6945e8924/drivers/r2dbc-driver/src/main/kotlin/app/cash/sqldelight/driver/r2dbc/R2dbcDriver.kt#L24
 */
private class R2dbcSqlService(
  private val connectionPool: ConnectionPool,
) : SqlService {
  private val closeTracker = CloseTracker()

  override suspend fun getOrCreate(name: String): SqlDatabase {
    require(name == "") {
      "SqlService doesn't support named databases yet"
    }

    return closeTracker.track { closeListener ->
      R2dbcSqlDatabase(connectionPool, closeListener)
    }
  }

  override fun close() {
    closeTracker.closeAll()
  }
}

private class R2dbcSqlDatabase(
  private val connectionPool: ConnectionPool,
  private val closeListener: CloseListener,
) : SqlDatabase {
  private val closeTracker = CloseTracker()

  override suspend fun newConnection(): SqlConnection {
    return closeTracker.track { closeListener ->
      R2dbcSqlConnection(connectionPool.create().awaitSingle(), closeListener)
    }
  }

  override fun close() {
    closeListener.onClose()
    closeTracker.closeAll()
  }
}

private class R2dbcSqlBinder(
  private val statement: Statement,
) : SqlBinder {
  override fun bindBool(index: Int, value: Boolean?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Boolean::class.javaObjectType)
    }
  }

  override fun bindS32(index: Int, value: Int?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Int::class.javaObjectType)
    }
  }

  override fun bindS64(index: Int, value: Long?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Long::class.javaObjectType)
    }
  }

  override fun bindF32(index: Int, value: Float?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Float::class.javaObjectType)
    }
  }

  override fun bindF64(index: Int, value: Double?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Double::class.javaObjectType)
    }
  }

  override fun bindChar(index: Int, value: Int?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, Int::class.javaObjectType)
    }
  }

  override fun bindInstant(index: Int, value: Instant?) {
    when {
      value != null -> statement.bind(index, value.toJavaInstant())
      else -> statement.bindNull(index, java.time.Instant::class.javaObjectType)
    }
  }

  override fun bindUuid(index: Int, value: Uuid?) {
    when {
      value != null -> statement.bind(index, value.toJavaUuid())
      else -> statement.bindNull(index, UUID::class.javaObjectType)
    }
  }

  override fun bindString(index: Int, value: String?) {
    when {
      value != null -> statement.bind(index, value)
      else -> statement.bindNull(index, String::class.java)
    }
  }

  override fun bindBytes(index: Int, value: ByteString?) {
    when {
      value != null -> statement.bind(index, value.toByteArray())
      else -> statement.bindNull(index, ByteArray::class.java)
    }
  }

  override fun bindJson(index: Int, value: JsonLiteral?) {
    when {
      value != null -> statement.bind(index, Json.of(value.json))
      else -> statement.bindNull(index, Json::class.javaObjectType)
    }
  }
}

private class R2dbcSqlConnection(
  private val connection: Connection,
  private val closeListener: CloseListener,
) : SqlConnection {
  private val closeTracker = CloseTracker()

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

    return closeTracker.track { closeListener ->
      statement
        .execute()
        .awaitSingle()
        .map { row, rowMetadata ->
          R2dbcSqlRow(
            Array(rowMetadata.columnMetadatas.size) { index ->
              row.get(index)
            },
          )
        }
        .asRowIterator(closeListener)
    }
  }

  override fun close() {
    closeListener.onClose()
    closeTracker.closeAll()
    connection.close().subscribeAndDiscard()
  }
}

private fun Publisher<R2dbcSqlRow>.asRowIterator(
  closeListener: CloseListener,
) = object : RowIterator {
  private val delegate = PublisherIterator(this@asRowIterator)

  override suspend fun next() = delegate.next()

  override fun close() {
    closeListener.onClose()
    delegate.close()
  }
}

private class R2dbcSqlRow(
  private val values: Array<Any?>,
) : SqlRow {
  override fun getBool(index: Int) = values[index] as Boolean?

  override fun getS32(index: Int) = values[index] as Int?

  override fun getS64(index: Int) = values[index] as Long?

  override fun getF32(index: Int) = values[index] as Float?

  override fun getF64(index: Int) = values[index] as Double?

  override fun getChar(index: Int) = values[index] as Int?

  override fun getInstant(index: Int) =
    (values[index] as Temporal?)?.let { java.time.Instant.from(it).toKotlinInstant() }

  override fun getString(index: Int) = values[index] as String?

  override fun getBytes(index: Int) = (values[index] as ByteBuffer?)?.toByteString()

  override fun getUuid(index: Int) = (values[index] as UUID?)?.toKotlinUuid()

  override fun getJson(index: Int) =
    (values[index] as Json?)?.let { JsonLiteral(it.asString()) }
}
