@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.OsScope
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.asSqlService
import com.wasmo.stripe.StripeCredentials
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import okio.ByteString

@Inject
@SingleIn(OsScope::class)
class WasmoService(
  private val server: EmbeddedServer<*, *>,
  private val actionRouter: ActionRouter,
) {
  fun start() {
    actionRouter.createRoutes()
    server.start(true)
  }

  data class Config(
    val cookieSecret: ByteString,
    val postmarkCredentials: PostmarkCredentials,
    val stripeCredentials: StripeCredentials,
    val catalog: Catalog,
    val osPostgresqlAddress: PostgresqlAddress,
    val applicationPostgresqlAddress: PostgresqlAddress,
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

  val osPostgresqlClient = PostgresqlClient(config.osPostgresqlAddress)
  val wasmoDb = osPostgresqlClient.asSqlService().getOrCreate()

  val applicationPostgresqlClient = PostgresqlClient(config.applicationPostgresqlAddress)
  val sqlService = applicationPostgresqlClient.asSqlService()

  val wasmoServiceGraphFactory = createGraphFactory<WasmoServiceGraph.Factory>()
  val serviceGraph = wasmoServiceGraphFactory.create(
    config = config,
    server = server,
    wasmoDb = wasmoDb,
    sqlService = sqlService,
  )

  serviceGraph.wasmoService.start()
  return serviceGraph.wasmoService
}
