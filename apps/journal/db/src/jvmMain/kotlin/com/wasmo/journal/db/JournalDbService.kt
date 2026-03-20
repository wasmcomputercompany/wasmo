package com.wasmo.journal.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import java.io.Closeable
import kotlin.time.Instant

class JournalDbService(
  private val driver: SqlDriver,
) : JournalDb by JournalDb.Companion(driver, PersonAdapter), Closeable by driver {

  suspend fun migrate() {
    JournalDb.Schema.migrate(driver, 0L, JournalDb.Schema.version).await()
  }

  companion object {
    private object InstantAdapter : ColumnAdapter<Instant, String> {
      override fun decode(databaseValue: String) = Instant.parse(databaseValue)
      override fun encode(value: Instant) = value.toString()
    }

    private object PersonIdAdapter : ColumnAdapter<PersonId, Long> {
      override fun decode(databaseValue: Long) = PersonId(databaseValue)
      override fun encode(value: PersonId) = value.id
    }

    private val PersonAdapter = Person.Adapter(
      idAdapter = PersonIdAdapter,
      created_atAdapter = InstantAdapter,
    )
  }
}
