@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.HmacChallenger
import com.wasmo.accounts.RealAccountStore
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieEncoder
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.api.routes.RoutingContext
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.catalog.Catalog
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.deployment.Deployment
import com.wasmo.http.RealHttpClient
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkEmailService
import com.wasmo.stripe.StripeCredentials
import com.wasmo.stripe.StripeInitializer
import com.wasmo.website.RealServerAppPage
import io.ktor.server.netty.EngineMain
import kotlin.time.Clock
import okhttp3.OkHttpClient
import okio.ByteString

class WasmoService(
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
) {
  fun start(args: Array<String>) {
    val server = EngineMain.createServer(args)

    val clock = Clock.System
    val wasmoDbService = WasmoDbService.start(
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
      clock = clock,
      client = okHttpClient,
    )
    val hmacChallengerFactory = HmacChallenger.Factory(
      clock = clock,
      cookieSecret = cookieSecret,
    )
    val cookieClientFactory = CookieClient.Factory(
      clock = clock,
      cookieQueries = wasmoDbService.cookieQueries,
      accountQueries = wasmoDbService.accountQueries,
      hmacChallengerFactory = hmacChallengerFactory,
    )
    val clientAuthenticatorFactory = RealClientAuthenticator.Factory(
      clock = clock,
      sessionCookieSpec = sessionCookieSpec,
      sessionCookieEncoder = SessionCookieEncoder(cookieSecret),
      cookieClientFactory = cookieClientFactory,
    )
    val authenticatorDatabase = RealAuthenticatorDatabase()
    val accountStoreFactory = RealAccountStore.Factory(
      authenticatorDatabase = authenticatorDatabase,
      wasmoDbService = wasmoDbService,
    )
    val rootObjectStore = objectStoreFactory.open(objectStoreAddress)
    val computerStore = RealComputerStore(
      deployment = deployment,
      clock = clock,
      rootObjectStore = rootObjectStore,
      httpClient = httpClient,
      objectStoreKeyFactory = ObjectStoreKeyFactory(),
      service = wasmoDbService,
    )
    val sendEmailService = PostmarkEmailService.Factory(
      credentials = postmarkCredentials,
      client = okHttpClient,
    ).create()
    val stripeInitializer = StripeInitializer(
      stripeCredentials = stripeCredentials,
    ).apply {
      initialize()
    }
    val passkeyLinkerFactory = PasskeyLinker.Factory(
      cookieQueries = wasmoDbService.cookieQueries,
    )
    val serverAppPageFactory = RealServerAppPage.Factory(
      deployment = deployment,
      stripePublishableKey = stripeInitializer.stripeCredentials.publishableKey,
    )
    val inviteService = InviteService(clock, wasmoDbService)
    val routeCodec = RealRouteCodec(
      RoutingContext(
        rootUrl = deployment.baseUrl.toString(),
        hasComputers = false,
        hasInvite = false,
        isAdmin = false,
      ),
    )
    val actionRouter = ActionRouter(
      clock = clock,
      deployment = deployment,
      application = server.application,
      clientAuthenticatorFactory = clientAuthenticatorFactory,
      accountStoreFactory = accountStoreFactory,
      passkeyLinkerFactory = passkeyLinkerFactory,
      computerStore = computerStore,
      sendEmailService = sendEmailService,
      stripeInitializer = stripeInitializer,
      catalog = catalog,
      wasmoDbService = wasmoDbService,
      serverAppPageFactory = serverAppPageFactory,
      inviteService = inviteService,
      routeCodec = routeCodec,
    )
    actionRouter.createRoutes()

    server.start(true)
  }
}

