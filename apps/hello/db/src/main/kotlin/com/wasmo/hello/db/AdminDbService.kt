package com.wasmo.hello.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import kotlin.time.Instant
import okio.Closeable
import okio.Path

class HelloDbService(
  private val driver: JdbcSqliteDriver,
) : HelloDb by HelloDb(driver, PersonAdapter), Closeable by driver {

  fun migrate() {
    val schema = HelloDb.Schema
    this.transactionWithResult(noEnclosing = true) {
      val version = driver.getVersion()

      if (version < schema.version) {
        schema.migrate(driver, version, schema.version).value
        driver.setVersion(schema.version)
      }
    }
  }

  companion object {
    fun open(path: Path) = HelloDbService(
      driver = JdbcSqliteDriver(
        url = "jdbc:sqlite:$path",
        properties = Properties(),
      ),
    )

    private object InstantAdapter : ColumnAdapter<Instant, String> {
      override fun decode(databaseValue: String) = Instant.parse(databaseValue)
      override fun encode(value: Instant): String = value.toString()
    }

    private object PersonIdAdapter : ColumnAdapter<PersonId, Long> {
      override fun decode(databaseValue: Long) = PersonId(databaseValue)
      override fun encode(value: PersonId) = value.id
    }

    private val PersonAdapter = Person.Adapter(
      idAdapter = PersonIdAdapter,
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
