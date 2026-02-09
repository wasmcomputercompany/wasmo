package com.wasmo.admin.api

import kotlinx.serialization.json.Json

val AdminJson = Json {
  this.ignoreUnknownKeys = true
}
