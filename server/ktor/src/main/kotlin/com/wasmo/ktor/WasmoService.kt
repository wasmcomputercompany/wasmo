@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieEncoder
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.http.RealHttpClient
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import io.ktor.server.netty.EngineMain
import kotlin.time.Clock
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okio.ByteString

class WasmoService(
  val cookieSecret: ByteString,
  val postgresDatabaseHostname: String,
  val postgresDatabaseName: String,
  val postgresDatabaseUser: String,
  val postgresDatabasePassword: String,
  val baseUrl: HttpUrl,
  val objectStoreAddress: ObjectStoreAddress,
  val sessionCookieSpec: SessionCookieSpec,
) {
  fun start(args: Array<String>) {
    val server = EngineMain.createServer(args)

    val clock = Clock.System
    val service = WasmoDbService.start(
      hostname = postgresDatabaseHostname,
      databaseName = postgresDatabaseName,
      user = postgresDatabaseUser,
      password = postgresDatabasePassword,
      ssl = false,
    )
    val okHttpClient = OkHttpClient()
    val httpClient = RealHttpClient(
      callFactory = okHttpClient,
    )
    val objectStoreFactory = ObjectStoreFactory(
      clock,
      okHttpClient,
    )
    val clientAuthenticatorFactory = RealClientAuthenticator.Factory(
      clock = clock,
      sessionCookieSpec = sessionCookieSpec,
      sessionCookieEncoder = SessionCookieEncoder(cookieSecret),
      cookieClientFactory = CookieClient.Factory(
        clock = clock,
        cookieQueries = service.cookieQueries,
        accountQueries = service.accountQueries,
      ),
    )
    val rootObjectStore = objectStoreFactory.open(objectStoreAddress)
    val computerStore = RealComputerStore(
      baseUrl = baseUrl,
      clock = clock,
      rootObjectStore = rootObjectStore,
      httpClient = httpClient,
      objectStoreKeyFactory = ObjectStoreKeyFactory(),
      service = service,
    )
    val actionRouter = ActionRouter(
      baseUrl = baseUrl,
      application = server.application,
      clientAuthenticatorFactory = clientAuthenticatorFactory,
      computerStore = computerStore,
    )
    actionRouter.createRoutes()

    server.start(true)
  }
}

