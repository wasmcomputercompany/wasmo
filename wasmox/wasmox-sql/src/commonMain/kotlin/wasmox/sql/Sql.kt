package wasmox.sql

import okio.use
import wasmo.sql.RowIterator
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlException
import wasmo.sql.SqlRow

suspend fun <T> SqlDatabase.transaction(
  attemptCount: Int = 1,
  block: suspend context(SqlTransaction) () -> T,
): T {
  check(attemptCount >= 1)
  var attempt = 0
  var suppressedExceptions = listOf<SqlException>()
  while (true) {
    attempt++
    val transaction = RealSqlTransaction(newConnection())
    val result = context(transaction) {
      transaction.use { transaction ->
        transaction.execute("BEGIN")
        val result = try {
          block()
        } catch (e: Throwable) {
          transaction.execute("ROLLBACK")

          // Race. Retry!
          if (attempt < attemptCount && e is SqlException && e.isUniqueViolation) {
            suppressedExceptions += e
            continue
          }

          for (exception in suppressedExceptions) {
            e.addSuppressed(exception)
          }

          throw e
        }
        transaction.execute("COMMIT")
        result
      }
    }

    for (action in transaction.afterCommitActions) {
      action()
    }

    return result
  }
}

suspend fun <T> SqlDatabase.withConnection(block: suspend context(SqlConnection) () -> T): T {
  newConnection().use { connection ->
    context(connection) {
      return block()
    }
  }
}

interface SqlTransaction : SqlConnection {
  val sqlConnection: SqlConnection

  fun afterCommit(function: () -> Unit)
}

internal class RealSqlTransaction(
  override val sqlConnection: SqlConnection,
) : SqlConnection by sqlConnection, SqlTransaction {
  val afterCommitActions = mutableListOf<() -> Unit>()

  override fun afterCommit(function: () -> Unit) {
    afterCommitActions += function
  }
}

suspend fun <T> RowIterator.list(mapper: SqlRow.() -> T): List<T> {
  use {
    return buildList {
      while (true) {
        val row = next() ?: break
        add(mapper(row))
      }
    }
  }
}

suspend fun <T> RowIterator.single(mapper: SqlRow.() -> T): T {
  use {
    val row = next() ?: error("expected one element but was none")
    val result = mapper(row)
    check(next() == null) { "expected one element but was multiple " }
    return result
  }
}

suspend fun <T> RowIterator.singleOrNull(mapper: SqlRow.() -> T): T? {
  use {
    val row = next() ?: return null
    val result = mapper(row)
    check(next() == null) { "expected at most one element but was multiple " }
    return result
  }
}

val SqlException.isUniqueViolation: Boolean
  get() = sqlState == "23505"
val SqlException.isDuplicateDatabase: Boolean
  get() = sqlState == "42P04"
