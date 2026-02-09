package com.wasmo.admin.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import kotlin.time.Instant
import okio.Closeable
import okio.Path

class AdminDbService(
  private val driver: JdbcSqliteDriver,
) : AdminDb by AdminDb(driver, AppInstallAdapter), Closeable by driver {

  fun migrate() {
    val schema = AdminDb.Schema
    this.transactionWithResult(noEnclosing = true) {
      val version = driver.getVersion()

      if (version < schema.version) {
        schema.migrate(driver, version, schema.version).value
        driver.setVersion(schema.version)
      }
    }
  }

  companion object {
    fun open(path: Path) = AdminDbService(
      driver = JdbcSqliteDriver(
        url = "jdbc:sqlite:$path",
        properties = Properties(),
      ),
    )

    private object InstantAdapter : ColumnAdapter<Instant, String> {
      override fun decode(databaseValue: String) = Instant.parse(databaseValue)
      override fun encode(value: Instant): String = value.toString()
    }

    private object AppInstallIdAdapter : ColumnAdapter<AppInstallId, Long> {
      override fun decode(databaseValue: Long) = AppInstallId(databaseValue)
      override fun encode(value: AppInstallId) = value.id
    }

    private val AppInstallAdapter = AppInstall.Adapter(
      idAdapter = AppInstallIdAdapter,
      created_atAdapter = InstantAdapter,
    )

    private fun JdbcSqliteDriver.getVersion(): Long {
      val mapper = { cursor: SqlCursor ->
        QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null)
      }
      return executeQuery(null, "PRAGMA user_version", mapper, 0, null).value ?: 0L
    }

    private fun JdbcSqliteDriver.setVersion(version: Long) {
      execute(null, "PRAGMA user_version = $version", 0, null).value
    }
  }
}
