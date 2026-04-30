@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.sql

import com.wasmo.support.closetracker.CloseListener
import com.wasmo.support.closetracker.CloseTracker
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.pgclient.PgException
import io.vertx.sqlclient.Row as VertxRow
import io.vertx.sqlclient.RowIterator as VertxRowIterator
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import io.vertx.sqlclient.data.NullValue
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import okio.ByteString
import okio.ByteString.Companion.toByteString
import wasmo.json.JsonLiteral
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlException
import wasmo.sql.SqlRow

fun PostgresqlClient.asSqlDatabase(): SqlDatabase = RealSqlDatabase(this)

internal class RealSqlDatabase(
  private val client: PostgresqlClient,
  private val closeListener: CloseListener? = null,
) : SqlDatabase {
  private val closeTracker = CloseTracker()

  override suspend fun newConnection(): SqlConnection {
    return closeTracker.track { closeListener ->
      RealSqlConnection(client.connect(), closeListener)
    }
  }

  override fun close() {
    closeListener?.onClose()
    closeTracker.closeAll()
    client.close()
  }
}

internal class RealSqlConnection(
  override val sqlClient: SqlClient,
  private val closeListener: CloseListener,
) : OsSqlConnection {
  private val closeTracker = CloseTracker()

  override suspend fun execute(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ) = executeInternal(sql, bindParameters).rowCount().toLong()

  override suspend fun executeQuery(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ) = RealRowIterator(executeInternal(sql, bindParameters).iterator())

  private suspend fun executeInternal(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ): RowSet<VertxRow?> {
    try {
      val future = when {
        bindParameters != null -> {
          val preparedQuery = sqlClient.preparedQuery(sql)
          val tupleBuilder = TupleBuilder()
          tupleBuilder.bindParameters()
          preparedQuery.execute(tupleBuilder.build())
        }

        else -> sqlClient.query(sql).execute()
      }

      return future.asDeferred().await()
    } catch (e: PgException) {
      throw e.toSqlException()
    }
  }

  override fun close() {
    closeListener.onClose()
    closeTracker.closeAll()
    sqlClient.close()
  }
}

internal class TupleBuilder : SqlBinder {
  private val values = mutableListOf<Any?>()

  private fun set(index: Int, value: Any) {
    while (values.size <= index) {
      values += null
    }
    values[index] = value
  }

  override fun bindBool(index: Int, value: Boolean?) {
    set(index, value ?: NullValue.Boolean)
  }

  override fun bindS32(index: Int, value: Int?) {
    set(index, value ?: NullValue.Integer)
  }

  override fun bindS64(index: Int, value: Long?) {
    set(index, value ?: NullValue.Long)
  }

  override fun bindF32(index: Int, value: Float?) {
    set(index, value ?: NullValue.Float)
  }

  override fun bindF64(index: Int, value: Double?) {
    set(index, value ?: NullValue.Double)
  }

  override fun bindInstant(index: Int, value: Instant?) {
    val offsetDateTime = value?.let { OffsetDateTime.ofInstant(it.toJavaInstant(), ZoneOffset.UTC) }
    set(index, offsetDateTime ?: NullValue.OffsetDateTime)
  }

  override fun bindString(index: Int, value: String?) {
    set(index, value ?: NullValue.String)
  }

  override fun bindBytes(index: Int, value: ByteString?) {
    set(index, value?.let { Buffer.buffer(it.toByteArray()) } ?: NullValue.Buffer)
  }

  override fun bindUuid(index: Int, value: Uuid?) {
    set(index, value?.toJavaUuid() ?: NullValue.UUID)
  }

  override fun bindJson(index: Int, value: JsonLiteral?) {
    val jsonValue = value?.let { Json.CODEC.fromString(it.json, Any::class.java) }
      ?: NullValue.JsonObject
    set(index, jsonValue)
  }

  fun build(): Tuple = Tuple.wrap(values)
}

internal class RealRowIterator(
  private val delegate: VertxRowIterator<VertxRow?>,
) : RowIterator {
  override suspend fun next(): SqlRow? {
    if (!delegate.hasNext()) return null
    return RealSqlRow(delegate.next()!!)
  }

  override fun close() {
  }
}

internal class RealSqlRow(
  private val delegate: VertxRow,
) : SqlRow {
  override fun getBool(index: Int): Boolean? {
    return delegate.getBoolean(index)
  }

  override fun getS32(index: Int): Int? {
    return delegate.getInteger(index)
  }

  override fun getS64(index: Int): Long? {
    return delegate.getLong(index)
  }

  override fun getF32(index: Int): Float? {
    return delegate.getFloat(index)
  }

  override fun getF64(index: Int): Double? {
    return delegate.getDouble(index)
  }

  override fun getInstant(index: Int): Instant? {
    val offsetDateTime = delegate.getOffsetDateTime(index) ?: return null
    return offsetDateTime.toInstant().toKotlinInstant()
  }

  override fun getString(index: Int): String? {
    return delegate.getString(index)
  }

  override fun getBytes(index: Int): ByteString? {
    val buffer = delegate.getBuffer(index) ?: return null
    return buffer.bytes.toByteString()
  }

  override fun getUuid(index: Int): Uuid? {
    return delegate.getUUID(index)?.toKotlinUuid()
  }

  override fun getJson(index: Int): JsonLiteral? {
    val json = delegate.getJson(index) ?: return null
    return JsonLiteral(Json.CODEC.toString(json))
  }
}

private fun PgException.toSqlException() = SqlException(
  message = message,
  sqlState = sqlState,
  detail = detail,
  hint = hint,
  position = position,
  where = where,
  schema = schema,
  table = table,
  column = column,
  dataType = dataType,
  constraint = constraint,
  file = file,
  line = line,
  routine = routine,
)
