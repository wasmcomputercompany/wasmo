@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.stripe.StripeCredentials
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import okio.ByteString
import com.wasmo.objectstore.ObjectStoreAddress

@Inject
@SingleIn(AppScope::class)
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
    val postgresDatabaseHostname: String,
    val postgresDatabaseName: String,
    val postgresDatabaseUser: String,
    val postgresDatabasePassword: String,
    val deployment: Deployment,
    val objectStoreAddress: ObjectStoreAddress,
    val sessionCookieSpec: SessionCookieSpec,
  )
}

fun startWasmoService(
  config: WasmoService.Config,
  args: Array<String>,
): WasmoService {
  val server = EngineMain.createServer(args)

  val wasmoDb = WasmoDbService.start(
    hostname = config.postgresDatabaseHostname,
    databaseName = config.postgresDatabaseName,
    user = config.postgresDatabaseUser,
    password = config.postgresDatabasePassword,
    ssl = false,
  )

  val wasmoServiceGraphFactory = createGraphFactory<WasmoServiceGraph.Factory>()
  val serviceGraph = wasmoServiceGraphFactory.create(
    config = config,
    server = server,
    wasmoDb = wasmoDb,
  )

  serviceGraph.wasmoService.start()
  return serviceGraph.wasmoService
}
