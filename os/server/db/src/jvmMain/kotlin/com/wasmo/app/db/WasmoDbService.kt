package com.wasmo.app.db

import app.cash.sqldelight.driver.jdbc.JdbcDriver
import com.wasmo.app.db2.RealWasmoDbConnection
import com.wasmo.app.db2.RealWasmoDbTransaction
import com.wasmo.app.db2.WasmoDbConnection
import com.wasmo.app.db2.WasmoDbTransaction
import java.io.Closeable
import org.apache.commons.dbcp2.PoolingDataSource
import wasmo.sql.SqlDatabase

class WasmoDbService(
  private val database: SqlDatabase,
  private val dataSource: PoolingDataSource<*>,
  private val jdbcDriver: JdbcDriver,
) : WasmoDb, Closeable {
  override suspend fun <T> transactionWithResult(
    noEnclosing: Boolean,
    block: suspend context(WasmoDbTransaction) () -> T,
  ): T {
    return transaction(noEnclosing, block)
  }

  override suspend fun <T> transaction(
    noEnclosing: Boolean,
    block: suspend context(WasmoDbTransaction) () -> T,
  ): T {
    val transaction = RealWasmoDbTransaction(database.newConnection())
    transaction.use { transaction ->
      context(transaction) {
        val result = block()
        for (action in transaction.afterCommitActions) {
          action()
        }
        return result
      }
    }
  }

  override suspend fun <T> withConnection(block: suspend context(WasmoDbConnection) () -> T): T {
    val connection = RealWasmoDbConnection(database.newConnection())
    connection.use { connection ->
      context(connection) {
        return block()
      }
    }
  }

  fun migrate(
    oldVersion: Long = 0L,
    newVersion: Long = Migrator.Schema.version,
  ) {
    Migrator.Schema.migrate(jdbcDriver, oldVersion, newVersion)
  }

  override fun close() {
    database.close()
    dataSource.close()
  }
}
