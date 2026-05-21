package com.wasmo.postgresqloperator

import okio.Path
import okio.Path.Companion.toPath

sealed interface LocalPostgresql {
  data object None : LocalPostgresql

  data class Exec(
    val postgres: Path = "/usr/libexec/postgresql18/postgres".toPath()
  ) : LocalPostgresql
}
