@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieEncoder
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.deployment.Deployment
import com.wasmo.http.RealHttpClient
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkEmailService
import io.ktor.server.netty.EngineMain
import kotlin.time.Clock
import okhttp3.OkHttpClient
import okio.ByteString

class WasmoService(
  val cookieSecret: ByteString,
  val postmarkCredentials: PostmarkCredentials,
  val postgresDatabaseHostname: String,
  val postgresDatabaseName: String,
  val postgresDatabaseUser: String,
  val postgresDatabasePassword: String,
  val deployment: Deployment,
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
      deployment = deployment,
      clock = clock,
      rootObjectStore = rootObjectStore,
      httpClient = httpClient,
      objectStoreKeyFactory = ObjectStoreKeyFactory(),
      service = service,
    )
    val sendEmailService = PostmarkEmailService.Factory(
      credentials = postmarkCredentials,
      client = okHttpClient,
    ).create()
    val actionRouter = ActionRouter(
      deployment = deployment,
      application = server.application,
      clientAuthenticatorFactory = clientAuthenticatorFactory,
      computerStore = computerStore,
      sendEmailService = sendEmailService,
    )
    actionRouter.createRoutes()

    server.start(true)
  }
}

