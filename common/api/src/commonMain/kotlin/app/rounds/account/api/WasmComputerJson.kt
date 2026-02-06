package app.rounds.account.api

import kotlinx.serialization.json.Json

val WasmComputerJson = Json {
  this.ignoreUnknownKeys = true
}
