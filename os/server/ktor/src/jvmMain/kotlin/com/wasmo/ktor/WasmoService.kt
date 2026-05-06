@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.common.catalog.Catalog
import com.wasmo.db.ensureSchemaVersion
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobProcessor
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.asSqlDatabase
import com.wasmo.stripe.StripeCredentials
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import okio.ByteString
import wasmox.sql.transaction

@Inject
@SingleIn(OsScope::class)
class WasmoService(
  private val server: EmbeddedServer<*, *>,
  private val actionRouter: ActionRouter,
  private val jobProcessor: JobProcessor,
) {
  fun start() {
    actionRouter.createRoutes()
    jobProcessor.start()
    // don't permanently block the calling thread
    server.start(wait = false)
  }

  data class Config(
    val cookieSecret: ByteString,
    val postmarkCredentials: PostmarkCredentials,
    val stripeCredentials: StripeCredentials,
    val catalog: Catalog,
    val osPostgresqlAddress: PostgresqlAddress,
    val provisioningPostgresqlAddress: PostgresqlAddress,
    val deployment: Deployment,
    val objectStoreAddress: ObjectStoreAddress,
    val sessionCookieSpec: SessionCookieSpec,
  )
}

suspend fun startWasmoService(
  config: WasmoService.Config,
  args: Array<String>,
): WasmoService {
  val server = EngineMain.createServer(args)

  val postgresqlClientFactory = PostgresqlClient.Factory()
  val osPostgresqlClient = postgresqlClientFactory.connect(config.osPostgresqlAddress)
  val provisioningDb = ProvisioningDb(
    address = config.provisioningPostgresqlAddress,
    provisioningDb = postgresqlClientFactory.connect(config.provisioningPostgresqlAddress)
      .asSqlDatabase(),
  )
  val wasmoDb = osPostgresqlClient.asSqlDatabase()

  wasmoDb.transaction {
    ensureSchemaVersion()
  }

  val serviceGraphFactory = createGraphFactory<ServiceGraph.Factory>()
  val serviceGraph = serviceGraphFactory.create(
    config = config,
    server = server,
    wasmoDb = wasmoDb,
    provisioningDb = provisioningDb,
  )

  serviceGraph.wasmoService.start()
  return serviceGraph.wasmoService
}
