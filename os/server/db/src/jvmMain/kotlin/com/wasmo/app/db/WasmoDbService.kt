package com.wasmo.app.db

import app.cash.sqldelight.driver.jdbc.JdbcDriver
import com.wasmo.app.db2.RealWasmoDbConnection
import com.wasmo.app.db2.WasmoDbTransaction
import java.io.Closeable
import org.apache.commons.dbcp2.PoolingDataSource
import wasmo.sql.SqlDatabase

class WasmoDbService(
  private val database: SqlDatabase,
  private val dataSource: PoolingDataSource<*>,
  private val jdbcDriver: JdbcDriver,
) : WasmoDb(), Closeable {
  override suspend fun newConnection() =
    RealWasmoDbConnection(database.newConnection())

  override suspend fun newTransaction(): WasmoDbTransaction =
    RealWasmoDbConnection(database.newConnection())

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
