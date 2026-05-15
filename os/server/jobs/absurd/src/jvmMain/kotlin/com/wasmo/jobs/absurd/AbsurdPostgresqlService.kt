package com.wasmo.jobs.absurd

import com.wasmo.identifiers.OsScope
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.support.absurd.PostgresqlClient
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.SslMode
import java.io.Closeable

/**
 * Singleton instance of Absurd's [PostgresqlClient]. This must be closed.
 */
@Inject
@SingleIn(OsScope::class)
internal class AbsurdPostgresqlService(
  val address: PostgresqlAddress,
) : Closeable {
  val client = PostgresqlClient(
    PgConnectOptions()
      .setHost(address.hostname)
      .setDatabase(address.databaseName)
      .setUser(address.user)
      .setPassword(address.password)
      .setSslMode(
        when {
          address.ssl -> SslMode.VERIFY_FULL
          else -> SslMode.DISABLE
        },
      ),
  )

  override fun close() {
    client.close()
  }
}
