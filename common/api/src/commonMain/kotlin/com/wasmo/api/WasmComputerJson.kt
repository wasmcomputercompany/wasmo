package com.wasmo.api

import kotlinx.serialization.json.Json

val WasmComputerJson = Json {
  this.ignoreUnknownKeys = true
}
