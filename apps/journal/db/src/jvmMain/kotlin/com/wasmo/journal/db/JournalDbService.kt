package com.wasmo.journal.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.wasmo.journal.api.Visibility
import java.io.Closeable
import kotlin.time.Instant

class JournalDbService(
  private val driver: SqlDriver,
) : JournalDb by JournalDb.Companion(
  driver,
  AttachmentAdapter,
  EntryAdapter,
), Closeable by driver {

  suspend fun migrate() {
    JournalDb.Schema.migrate(driver, 0L, JournalDb.Schema.version).await()
  }

  companion object {
    private object InstantAdapter : ColumnAdapter<Instant, String> {
      override fun decode(databaseValue: String) = Instant.parse(databaseValue)
      override fun encode(value: Instant) = value.toString()
    }

    private object AttachmentIdAdapter : ColumnAdapter<AttachmentId, Long> {
      override fun decode(databaseValue: Long) = AttachmentId(databaseValue)
      override fun encode(value: AttachmentId) = value.id
    }

    private object EntryIdAdapter : ColumnAdapter<EntryId, Long> {
      override fun decode(databaseValue: Long) = EntryId(databaseValue)
      override fun encode(value: EntryId) = value.id
    }

    private val AttachmentAdapter = Attachment.Adapter(
      idAdapter = AttachmentIdAdapter,
      posted_atAdapter = InstantAdapter,
    )

    private val EntryAdapter = Entry.Adapter(
      idAdapter = EntryIdAdapter,
      dateAdapter = InstantAdapter,
      visibilityAdapter = EnumColumnAdapter<Visibility>(),
    )
  }
}
