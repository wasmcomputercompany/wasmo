package com.wasmo.api

import kotlinx.serialization.json.Json

val WasmoJson = Json {
  this.ignoreUnknownKeys = true
}
