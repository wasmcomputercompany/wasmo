package com.wasmo.wiring

import com.wasmo.db.ensureSchemaVersion
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.asSqlDatabase
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

/**
 * Each subclass is its own particular distribution of Wasmo OS.
 */
abstract class Distribution {
  protected abstract val osPostgresqlAddress: PostgresqlAddress
  protected abstract val provisioningPostgresqlAddress: PostgresqlAddress

  protected abstract fun createService(
    server: EmbeddedServer<*, *>,
    provisioningDb: ProvisioningDb,
    wasmoDb: SqlDatabase,
  ): WasmoService

  suspend fun start(args: Array<String>) {
    val server = EngineMain.createServer(args)

    val postgresqlClientFactory = PostgresqlClient.Factory()
    val osPostgresqlClient = postgresqlClientFactory.connect(osPostgresqlAddress)
    val provisioningDb = ProvisioningDb(
      address = provisioningPostgresqlAddress,
      provisioningDb = postgresqlClientFactory.connect(provisioningPostgresqlAddress)
        .asSqlDatabase(),
    )
    val wasmoDb = osPostgresqlClient.asSqlDatabase()

    wasmoDb.transaction {
      ensureSchemaVersion()
    }

    val wasmoService = createService(
      server = server,
      provisioningDb = provisioningDb,
      wasmoDb = wasmoDb,
    )

    wasmoService.start()
  }
}
