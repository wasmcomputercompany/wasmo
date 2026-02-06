package com.publicobject.wasmcomputer.api

import kotlinx.serialization.json.Json

val WasmComputerJson = Json {
  this.ignoreUnknownKeys = true
}
