package com.wasmo.postgresqloperator

sealed interface LocalPostgresql {
  data object None : LocalPostgresql

  data object Exec : LocalPostgresql
}
