package com.wasmo.jobs.absurd

import com.wasmo.identifiers.OsScope
import com.wasmo.sql.WasmoPostgresqlConfig
import dev.wasmo.absurd.PostgresqlClient
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
  val postgresqlConfig: WasmoPostgresqlConfig,
) : Closeable {
  val client = PostgresqlClient(
    PgConnectOptions()
      .setHost(postgresqlConfig.hostname)
      .setPort(postgresqlConfig.port)
      .setDatabase(postgresqlConfig.osDatabaseName)
      .setUser(postgresqlConfig.osUser)
      .setPassword(postgresqlConfig.osPassword.rawUnredactedSecret)
      .setSslMode(
        when {
          postgresqlConfig.ssl -> SslMode.VERIFY_FULL
          else -> SslMode.DISABLE
        },
      ),
  )

  override fun close() {
    client.close()
  }
}
