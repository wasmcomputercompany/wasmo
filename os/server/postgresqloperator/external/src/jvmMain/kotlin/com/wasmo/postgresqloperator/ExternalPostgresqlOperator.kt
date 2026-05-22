package com.wasmo.postgresqloperator

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope

/**
 * Don't do anything to set up Postgresql; it's handled externally.
 */
@Inject
@SingleIn(OsScope::class)
internal class ExternalPostgresqlOperator : PostgresqlOperator {
  context(scope: CoroutineScope)
  override suspend fun await() {
  }
}
