package com.wasmo.postgresqloperator

import kotlinx.coroutines.CoroutineScope

/**
 * Manage connectivity to Postgresql.
 */
interface PostgresqlOperator {
  /** Returns when Postgresql is receiving connections. */
  context(scope: CoroutineScope)
  suspend fun await()
}
