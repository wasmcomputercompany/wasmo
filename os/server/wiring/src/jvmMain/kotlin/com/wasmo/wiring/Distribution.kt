package com.wasmo.wiring

import com.wasmo.db.Migrator
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.asSqlDatabase
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

interface DistributionGraph {
  val wasmoService: WasmoService
  val migrator: Migrator
}

/**
 * Each subclass is its own particular distribution of Wasmo OS.
 */
abstract class Distribution {
  protected abstract val osPostgresqlAddress: PostgresqlAddress
  protected abstract val provisioningPostgresqlAddress: PostgresqlAddress

  protected abstract fun createServiceGraph(
    server: EmbeddedServer<*, *>,
    provisioningDb: ProvisioningDb,
    wasmoDb: SqlDatabase,
  ): DistributionGraph

  interface WasmoServiceControl : AutoCloseable {
    suspend fun start()
  }

  private suspend fun commonInit(args: Array<String>): WasmoServiceControl {
    val server = EngineMain.createServer(args)

    val postgresqlClientFactory = PostgresqlClient.Factory()
    val osPostgresqlClient = postgresqlClientFactory.connect(osPostgresqlAddress)
    val provisioningDb = ProvisioningDb(
      address = provisioningPostgresqlAddress,
      provisioningDb = postgresqlClientFactory.connect(provisioningPostgresqlAddress)
        .asSqlDatabase(),
    )
    val wasmoDb = osPostgresqlClient.asSqlDatabase()
    val serviceGraph = createServiceGraph(
      server = server,
      provisioningDb = provisioningDb,
      wasmoDb = wasmoDb,
    )
    wasmoDb.transaction {
      serviceGraph.migrator.ensureSchemaVersion()
    }
    return object : WasmoServiceControl {
      override suspend fun start() {
        serviceGraph.wasmoService.start()
      }

      override fun close() {
        wasmoDb.close()
        provisioningDb.provisioningDb.close()
        osPostgresqlClient.close()
      }
    }
  }

  suspend fun startWasmoService(args: Array<String>) {
    val wasmoServiceStarter = commonInit(args)
    // start, never close()
    wasmoServiceStarter.start()
  }

  suspend fun prestartWasmoServiceAndExit(args: Array<String>) {
    commonInit(args).use {
      // nothing
    } // close() without having started
  }
}
