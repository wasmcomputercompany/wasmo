package com.wasmo.journal.api

import kotlinx.serialization.json.Json

val JournalJson = Json {
  this.ignoreUnknownKeys = true
}
