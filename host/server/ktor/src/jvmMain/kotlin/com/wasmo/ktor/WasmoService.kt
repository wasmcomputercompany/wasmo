@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.stripe.StripeClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.invite.InviteService
import com.wasmo.accounts.passkeys.PasskeyLinker
import com.wasmo.api.routes.RouteCodec
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.RealCallDataService
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.computers.SubscriptionUpdater
import com.wasmo.http.HttpClient
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.sendemail.postmark.PostmarkEmailService
import com.wasmo.stripe.StripePaymentsService
import com.wasmo.website.RealServerHostPage
import dev.zacsweers.metro.Inject
import io.ktor.server.engine.EmbeddedServer
import kotlin.time.Clock
import okhttp3.OkHttpClient

@Inject
class WasmoService(
  private val config: WasmoServiceConfig,
  private val server: EmbeddedServer<*, *>,
  private val wasmoDbService: WasmoDbService,
  private val clock: Clock,
  private val okHttpClient: OkHttpClient,
  private val httpClient: HttpClient,
  private val objectStoreFactory: ObjectStoreFactory,
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val routeCodecFactory: RouteCodec.Factory,
) {
  fun start() {
    val authenticatorDatabase = RealAuthenticatorDatabase()
    val callDataServiceFactory = RealCallDataService.Factory(
      deployment = config.deployment,
      routeCodecFactory = routeCodecFactory,
      authenticatorDatabase = authenticatorDatabase,
      wasmoDbService = wasmoDbService,
    )
    val rootObjectStore = objectStoreFactory.open(config.objectStoreAddress)
    val computerStore = RealComputerStore(
      deployment = config.deployment,
      clock = clock,
      rootObjectStore = rootObjectStore,
      httpClient = httpClient,
      objectStoreKeyFactory = ObjectStoreKeyFactory(),
      wasmoDbService = wasmoDbService,
    )
    val sendEmailService = PostmarkEmailService.Factory(
      credentials = config.postmarkCredentials,
      client = okHttpClient,
    ).create()
    val stripeClient = StripeClient.StripeClientBuilder()
      .setApiKey(config.stripeCredentials.secretKey)
      .build()
    val passkeyLinkerFactory = PasskeyLinker.Factory(
      cookieQueries = wasmoDbService.cookieQueries,
    )
    val serverAppPageFactory = RealServerHostPage.Factory(
      deployment = config.deployment,
      stripePublishableKey = config.stripeCredentials.publishableKey,
    )
    val inviteService = InviteService(
      clock = clock,
      wasmoDbService = wasmoDbService,
    )
    val paymentsService = StripePaymentsService(
      deployment = config.deployment,
      sessionService = stripeClient.v1().checkout().sessions(),
      subscriptionService = stripeClient.v1().subscriptions(),
      catalog = config.catalog,
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
      deployment = config.deployment,
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

