@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.common.catalog.Catalog
import com.wasmo.db.ensureSchemaVersion
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.OsScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.ByteString
import wasmox.sql.transaction

@Inject
@SingleIn(OsScope::class)
class WasmoService(
  private val server: EmbeddedServer<*, *>,
  private val actionRouter: ActionRouter,
) {
  fun start() {
    actionRouter.createRoutes()
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

  // On plain JVM outside of UI toolkits, the main thread appears to not have
  // a coroutine dispatcher that execution can return to.
  //
  // withContext(Dispatchers.IO) ensures that remaining execution can at least
  // return to one of plentiful I/O threads; else, it'd continue on whatever
  // thread it had migrated to (empirically, the Vert.x eventloop thread).
  withContext(Dispatchers.IO) {
    wasmoDb.transaction {
      ensureSchemaVersion()
    }
  }

  val wasmoServiceGraphFactory = createGraphFactory<WasmoServiceGraph.Factory>()
  val serviceGraph = wasmoServiceGraphFactory.create(
    config = config,
    server = server,
    wasmoDb = wasmoDb,
    provisioningDb = provisioningDb,
  )

  serviceGraph.wasmoService.start()
  return serviceGraph.wasmoService
}
