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
    @Language("SQL") sql: String,
    bindParameters: (SqlBinder.() -> Unit)? = null,
  ): Long

  /** Return the rows in a SELECT clause. */
  suspend fun executeQuery(
    @Language("SQL") sql: String,
    bindParameters: (SqlBinder.() -> Unit)? = null,
  ): RowIterator
}

/**
 * See the Postgresql docs for an explanation of each of these properties.
 * https://www.postgresql.org/docs/current/protocol-error-fields.html
 */
open class SqlException(
  message: String?,
  /** https://www.postgresql.org/docs/current/errcodes-appendix.html */
  val sqlState: String? = null,
  val detail: String? = null,
  val hint: String? = null,
  val position: String? = null,
  val where: String? = null,
  val schema: String? = null,
  val table: String? = null,
  val column: String? = null,
  val dataType: String? = null,
  val constraint: String? = null,
  val file: String? = null,
  val line: String? = null,
  val routine: String? = null,
) : Exception(message)

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
  fun bindInstant(index: Int, value: Instant?)
  fun bindString(index: Int, value: String?)
  fun bindBytes(index: Int, value: ByteString?)
  fun bindUuid(index: Int, value: Uuid?)
  fun bindJson(index: Int, value: JsonLiteral?)
}

/** We use `@Language` to get syntax highlighting for SQL in IntelliJ. */
internal expect annotation class Language(
  val value: String,
  val prefix: String = "",
  val suffix: String = "",
)
