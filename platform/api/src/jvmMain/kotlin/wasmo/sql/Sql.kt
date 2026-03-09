package wasmo.sql

import okio.ByteString

/**
 * Creates PostgreSQL databases and executes writes and reads on them.
 */
interface SqlSessionFactory {
  /**
   * Connect to the named database, creating it if necessary. Use 'null' for the application's
   * default database.
   */
  suspend fun connect(name: String? = null): SqlSession
}

/**
 * Writes on the root session are applied immediately.
 */
interface SqlRootSession : SqlSession {
  suspend fun beginTransaction(): SqlTransaction

  /**
   * Disconnect this session and release its resources. Any in-flight transactions will be aborted.
   */
  fun close()
}

/**
 * Writes on a transaction are staged and not applied until [commit].
 */
interface SqlTransaction : SqlSession {
  suspend fun commit()
  suspend fun rollback()
}

interface SqlSession {
  /** Return the number of rows in an UPDATE, INSERT, or DELETE clause. */
  suspend fun execute(
    sql: String,
    parameterCount: Int,
    binder: SqlBinder,
  ): Long

  /** Return the rows in a SELECT clause. */
  suspend fun executeQuery(
    sql: String,
    parameterCount: Int,
    binder: SqlBinder,
  ): RowIterator
}

class SqlException(message: String?): Exception(message)

interface RowIterator {
  /** Returns null if there is no next  row. */
  suspend fun next(): SqlRow?

  fun close()
}

interface SqlRow {
  fun getString(columnIndex: Int): String?
  fun getLong(columnIndex: Int): Long?
  fun getBytes(columnIndex: Int): ByteString?
  fun getDouble(columnIndex: Int): Double?
  fun getBoolean(columnIndex: Int): Boolean?
}

interface SqlBinder {
  fun bindString(index: Int, string: String?)
  fun bindLong(index: Int, long: Long?)
  fun bindBytes(index: Int, bytes: ByteString?)
  fun bindDouble(index: Int, double: Double?)
  fun bindBoolean(index: Int, boolean: Boolean?)
}
