package wasmo.sql

import okio.ByteString
import okio.Closeable

/**
 * Creates PostgreSQL databases and executes writes and reads on them.
 */
interface SqlService {
  /**
   * Connect to the named database, creating it if necessary. Use 'null' for the application's
   * default database.
   */
  suspend fun getOrCreate(name: String? = null): SqlDatabase
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

class SqlException(message: String?) : Exception(message)

interface RowIterator : Closeable {
  /** Returns null if there is no next row. */
  suspend fun next(): SqlRow?
}

interface SqlRow {
  fun getString(columnIndex: Int): String?
  fun getLong(columnIndex: Int): Long?
  fun getBytes(columnIndex: Int): ByteString?
  fun getDouble(columnIndex: Int): Double?
  fun getBoolean(columnIndex: Int): Boolean?
}

interface SqlBinder {
  fun bindString(index: Int, value: String?)
  fun bindLong(index: Int, value: Long?)
  fun bindBytes(index: Int, value: ByteString?)
  fun bindDouble(index: Int, value: Double?)
  fun bindBoolean(index: Int, value: Boolean?)
}
