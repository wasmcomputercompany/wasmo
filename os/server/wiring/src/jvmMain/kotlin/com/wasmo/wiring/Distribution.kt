package com.wasmo.wiring

import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.WasmoPostgresqlConfig
import com.wasmo.sql.asSqlDatabase
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.CoroutineScope
import wasmo.sql.SqlDatabase

/**
 * Each subclass is its own particular distribution of Wasmo OS.
 */
abstract class Distribution {
  protected abstract val postgresqlConfig: WasmoPostgresqlConfig

  protected abstract fun createService(
    server: EmbeddedServer<*, *>,
    provisioningDb: ProvisioningDb,
    wasmoDb: SqlDatabase,
  ): WasmoService

  context(scope: CoroutineScope)
  suspend fun start() {
    val server = EngineMain.createServer(arrayOf())

    val postgresqlClientFactory = PostgresqlClient.Factory()
    val osPostgresqlClient = postgresqlClientFactory.connect(postgresqlConfig.os)
    val provisioningDb = ProvisioningDb(
      address = postgresqlConfig.admin,
      provisioningDb = postgresqlClientFactory.connect(postgresqlConfig.admin)
        .asSqlDatabase(),
    )
    val wasmoDb = osPostgresqlClient.asSqlDatabase()
    val wasmoService = createService(
      server = server,
      provisioningDb = provisioningDb,
      wasmoDb = wasmoDb,
    )

    wasmoService.start()
  }
}
