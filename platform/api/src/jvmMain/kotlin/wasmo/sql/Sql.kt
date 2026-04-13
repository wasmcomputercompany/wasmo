@file:OptIn(ExperimentalUuidApi::class)

package wasmo.sql

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import okio.ByteString
import okio.Closeable
import wasmo.json.JsonLiteral

/**
 * Creates PostgreSQL databases and executes writes and reads on them.
 */
interface SqlService : Closeable {
  /**
   * Connect to the named database, creating it if necessary. Use '""' for the application's
   * default database.
   */
  suspend fun getOrCreate(name: String = ""): SqlDatabase
}

/**
 * Closing a database cancels all in-flight calls.
 */
interface SqlDatabase : Closeable {
  suspend fun newConnection(): SqlConnection
}

interface SqlConnection : Closeable {
  /** Return the number of rows in an UPDATE, INSERT, or DELETE clause. */
  suspend fun execute(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)? = null,
  ): Long

  /** Return the rows in a SELECT clause. */
  suspend fun executeQuery(
    sql: String,
    bindParameters: (SqlBinder.() -> Unit)? = null,
  ): RowIterator
}

open class SqlException(
  message: String?,
) : Exception(message)

class ConstraintViolationException(
  val constraintName: String,
) : SqlException("violated constraint: $constraintName")

interface RowIterator : Closeable {
  /** Returns null if there is no next row. */
  suspend fun next(): SqlRow?
}

interface SqlRow {
  fun getBool(index: Int): Boolean?
  fun getS32(index: Int): Int?
  fun getS64(index: Int): Long?
  fun getF32(index: Int): Float?
  fun getF64(index: Int): Double?
  fun getChar(index: Int): Int?
  fun getInstant(index: Int): Instant?
  fun getString(index: Int): String?
  fun getBytes(index: Int): ByteString?
  fun getUuid(index: Int): Uuid?
  fun getJson(index: Int): JsonLiteral?
}

interface SqlBinder {
  fun bindBool(index: Int, value: Boolean?)
  fun bindS32(index: Int, value: Int?)
  fun bindS64(index: Int, value: Long?)
  fun bindF32(index: Int, value: Float?)
  fun bindF64(index: Int, value: Double?)
  fun bindChar(index: Int, value: Int?)
  fun bindInstant(index: Int, value: Instant?)
  fun bindString(index: Int, value: String?)
  fun bindBytes(index: Int, value: ByteString?)
  fun bindUuid(index: Int, value: Uuid?)
  fun bindJson(index: Int, value: JsonLiteral?)
}
