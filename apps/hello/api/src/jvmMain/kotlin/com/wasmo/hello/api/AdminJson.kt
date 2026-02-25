package com.wasmo.hello.api

import kotlinx.serialization.json.Json

val HelloJson = Json {
  this.ignoreUnknownKeys = true
}
