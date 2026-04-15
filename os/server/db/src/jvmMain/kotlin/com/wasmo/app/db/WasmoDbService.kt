package com.wasmo.app.db

import app.cash.sqldelight.driver.jdbc.JdbcDriver
import java.io.Closeable
import org.apache.commons.dbcp2.PoolingDataSource
import wasmo.sql.SqlConnection
import wasmo.sql.SqlDatabase

class WasmoDbService(
  private val database: SqlDatabase,
  private val dataSource: PoolingDataSource<*>,
  private val jdbcDriver: JdbcDriver,
) : WasmoDb, Closeable {
  override suspend fun <T> transactionWithResult(
    noEnclosing: Boolean,
    block: suspend context(SqlTransaction) () -> T,
  ): T {
    return transaction(noEnclosing, block)
  }

  override suspend fun <T> transaction(
    noEnclosing: Boolean,
    block: suspend context(SqlTransaction) () -> T,
  ): T {
    val transaction = RealSqlTransaction(database.newConnection())
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

  override suspend fun <T> withConnection(block: suspend context(SqlConnection) () -> T): T {
    database.newConnection().use { connection ->
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
