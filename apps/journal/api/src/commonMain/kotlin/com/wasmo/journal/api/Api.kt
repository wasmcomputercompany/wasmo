package com.wasmo.journal.api

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntrySnapshot(
  val token: String,
  val version: Long,
  val visibility: Visibility,
  val slug: String,
  val title: String,
  val date: Instant,
  val body: String,
  val attachments: List<AttachmentSnapshot> = listOf(),
)

@Serializable
data class SaveEntryRequest(
  val expectedVersion: Long,
  val entry: EntrySnapshot,
)

@Serializable
data class SaveEntryResponse(
  val error: SaveEntryError? = null,
)

@Serializable
sealed class SaveEntryError {
  @Serializable
  @SerialName("StaleVersionError")
  data object StaleVersionError : SaveEntryError()

  @Serializable
  @SerialName("SlugConflict")
  data object SlugConflict : SaveEntryError()
}

@Serializable
data class AttachmentSnapshot(
  val token: String,
)

enum class Visibility {
  Private,
  Deleted,
  Published,
}

@Serializable
data class EntrySummary(
  val token: String,
  val visibility: Visibility,
  val slug: String,
  val title: String,
  val date: Instant,
)

@Serializable
data class ListEntriesRequest(
  val offset: String? = null,
)

@Serializable
data class ListEntriesResponse(
  val entries: List<EntrySummary>,
  val nextRequest: ListEntriesRequest? = null,
)
