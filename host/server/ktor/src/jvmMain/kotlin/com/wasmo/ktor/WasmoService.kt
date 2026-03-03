@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.stripe.StripeClient
import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.HmacChallenger
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieEncoder
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.api.routes.RoutingContext
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.RealCallDataService
import com.wasmo.common.catalog.Catalog
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.computers.SubscriptionUpdater
import com.wasmo.deployment.Deployment
import com.wasmo.http.RealHttpClient
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkEmailService
import com.wasmo.stripe.StripeCredentials
import com.wasmo.stripe.StripePaymentsService
import com.wasmo.website.RealServerHostPage
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
      deployment = deployment,
      sessionCookieSpec = sessionCookieSpec,
      sessionCookieEncoder = SessionCookieEncoder(cookieSecret),
      cookieClientFactory = cookieClientFactory,
    )
    val routeCodecFactory = RealRouteCodec.Factory()
    val authenticatorDatabase = RealAuthenticatorDatabase()
    val callDataServiceFactory = RealCallDataService.Factory(
      deployment = deployment,
      routeCodecFactory = routeCodecFactory,
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
      wasmoDbService = wasmoDbService,
    )
    val sendEmailService = PostmarkEmailService.Factory(
      credentials = postmarkCredentials,
      client = okHttpClient,
    ).create()
    val stripeClient = StripeClient.StripeClientBuilder()
      .setApiKey(stripeCredentials.secretKey)
      .build()
    val passkeyLinkerFactory = PasskeyLinker.Factory(
      cookieQueries = wasmoDbService.cookieQueries,
    )
    val serverAppPageFactory = RealServerHostPage.Factory(
      deployment = deployment,
      stripePublishableKey = stripeCredentials.publishableKey,
    )
    val inviteService = InviteService(
      clock = clock,
      wasmoDbService = wasmoDbService,
    )
    val paymentsService = StripePaymentsService(
      deployment = deployment,
      sessionService = stripeClient.v1().checkout().sessions(),
      subscriptionService = stripeClient.v1().subscriptions(),
      catalog = catalog,
    )
    val computerSpecStore = ComputerSpecStore(
      clock = clock,
      wasmoDbService = wasmoDbService,
    )
    val subscriptionUpdater = SubscriptionUpdater(
      clock = clock,
      paymentsService = paymentsService,
      wasmoDbService = wasmoDbService,
      computerSpecStore = computerSpecStore
    )
    val actionRouter = ActionRouter(
      clock = clock,
      deployment = deployment,
      application = server.application,
      clientAuthenticatorFactory = clientAuthenticatorFactory,
      callDataServiceFactory = callDataServiceFactory,
      passkeyLinkerFactory = passkeyLinkerFactory,
      computerStore = computerStore,
      sendEmailService = sendEmailService,
      wasmoDbService = wasmoDbService,
      serverHostPageFactory = serverAppPageFactory,
      inviteService = inviteService,
      subscriptionUpdater = subscriptionUpdater,
      paymentsService = paymentsService,
      computerSpecStore = computerSpecStore,
    )
    actionRouter.createRoutes()

    server.start(true)
  }
}

