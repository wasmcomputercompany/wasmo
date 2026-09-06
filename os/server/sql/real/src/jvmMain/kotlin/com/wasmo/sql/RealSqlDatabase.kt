@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.sql

import com.wasmo.api.SqlExecuteStartedEvent
import com.wasmo.events.EventListener
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
import java.util.UUID
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import okio.ByteString
import okio.ByteString.Companion.toByteString
import wasmo.json.JsonLiteral
import wasmo.sql.RowIterator
import wasmo.sql.SqlBinder
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlException
import wasmo.sql.SqlRow
import wit.wasi.clocks.v0_2_0.WallClock
import wit.wasmo.sql.SqlRow as WasmoSqlRow
import wit.wasmo.sql.SqlValue
import wit.wasmo.uuid.Uuid as WasmoUuid

fun PostgresqlClient.asSqlDatabase(): SqlDatabase =
  RealSqlDatabase(
    client = this,
    eventListener = eventListener,
  )

internal class RealSqlDatabase(
  private val client: PostgresqlClient,
  private val closeListener: CloseListener? = null,
  private val eventListener: EventListener,
) : SqlDatabase {
  private val closeTracker = CloseTracker()

  override suspend fun newConnection(): SqlConnection {
    return closeTracker.track { closeListener ->
      RealSqlConnection(
        sqlClient = client.connect(),
        closeListener = closeListener,
        vertxRowMetadataHack = client.vertxRowMetadataHack,
        eventListener = eventListener,
      )
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
  private val vertxRowMetadataHack: VertxRowMetadataHack,
  private val eventListener: EventListener,
) : OsSqlConnection {
  private val closeTracker = CloseTracker()

  override suspend fun execute(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ) = preserveStackTrace {
    executeInternal(sql, bindParameters).rowCount().toLong()
  }

  override suspend fun executeQuery(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)?,
  ) = preserveStackTrace {
    val rowSet = executeInternal(sql, bindParameters)
    RealRowIterator(vertxRowMetadataHack, rowSet.iterator())
  }

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
          eventListener.onEvent(SqlExecuteStartedEvent(sql, tupleBuilder.values))
          preparedQuery.execute(tupleBuilder.build())
        }

        else -> {
          eventListener.onEvent(SqlExecuteStartedEvent(sql, emptyList()))
          sqlClient.query(sql).execute()
        }
      }

      return future.awaitSuspending()
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
  private val _values = mutableListOf<Any?>()
  public val values: List<Any?> = _values

  private fun set(index: Int, value: Any) {
    while (_values.size <= index) {
      _values += null
    }
    _values[index] = value
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

  fun build(): Tuple = Tuple.wrap(_values)
}

internal class RealRowIterator(
  private val vertxRowMetadataHack: VertxRowMetadataHack,
  private val delegate: VertxRowIterator<VertxRow?>,
) : RowIterator {
  override suspend fun next(): SqlRow? {
    if (!delegate.hasNext()) return null
    val row = delegate.next()!!
    val wasmoRow = vertxRowToWasmoSqlRow(row)
    return RealSqlRow(wasmoRow)
  }

  fun vertxRowToWasmoSqlRow(row: VertxRow): WasmoSqlRow {
    val columnDescriptors = vertxRowMetadataHack.getColumnDescriptors(row)
    val sqlValues = List(row.size()) { c ->
      val value = row.get(Any::class.java, c) ?: return@List null
      when (val typeName = columnDescriptors[c].typeName()) {
        "BOOL" -> SqlValue.Bool(value as Boolean)
        "INT4" -> SqlValue.S32(value as Int)
        "INT8" -> SqlValue.S64(value as Long)
        "FLOAT4" -> SqlValue.F32(value as Float)
        "FLOAT8" -> SqlValue.F64(value as Double)
        "TIMESTAMPTZ" -> {
          val instant = (value as OffsetDateTime).toInstant()
          SqlValue.Datetime(
            WallClock.Datetime(
              seconds = instant.epochSecond.toULong(),
              nanoseconds = instant.nano.toUInt(),
            ),
          )
        }

        "TEXT", "VARCHAR" -> SqlValue.String(value as String)
        "BYTEA" -> SqlValue.Bytes((value as Buffer).bytes.toByteString())
        "UUID" -> {
          value as UUID
          SqlValue.Uuid(
            WasmoUuid(value.mostSignificantBits.toULong() to value.leastSignificantBits.toULong()),
          )
        }

        "JSONB" -> SqlValue.Json(wit.wasmo.json.JsonLiteral(Json.CODEC.toString(value)))
        else -> error("value type not implemented: $typeName")
      }
    }

    return WasmoSqlRow(sqlValues)
  }

  override fun close() {
  }
}

internal class RealSqlRow(
  private val delegate: WasmoSqlRow,
) : SqlRow {
  override fun getBool(index: Int) =
    (delegate.value[index] as? SqlValue.Bool)?.value

  override fun getS32(index: Int) =
    (delegate.value[index] as? SqlValue.S32)?.value

  override fun getS64(index: Int) =
    (delegate.value[index] as? SqlValue.S64)?.value

  override fun getF32(index: Int) =
    (delegate.value[index] as? SqlValue.F32)?.value

  override fun getF64(index: Int) =
    (delegate.value[index] as? SqlValue.F64)?.value

  override fun getInstant(index: Int): Instant? {
    val datetime = (delegate.value[index] as? SqlValue.Datetime)?.value ?: return null
    return Instant.fromEpochSeconds(
      epochSeconds = datetime.seconds.toLong(),
      nanosecondAdjustment = datetime.nanoseconds.toInt(),
    )
  }

  override fun getString(index: Int) =
    (delegate.value[index] as? SqlValue.String)?.value

  override fun getBytes(index: Int) =
    (delegate.value[index] as? SqlValue.Bytes)?.value

  override fun getUuid(index: Int): Uuid? {
    val (v1, v2) = (delegate.value[index] as? SqlValue.Uuid)?.value?.value ?: return null
    return Uuid.fromULongs(v1, v2)
  }

  override fun getJson(index: Int): JsonLiteral? {
    val value = (delegate.value[index] as? SqlValue.Json)?.value ?: return null
    return JsonLiteral(value.value)
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
