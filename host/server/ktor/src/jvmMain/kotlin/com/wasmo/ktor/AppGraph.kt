package com.wasmo.ktor

import com.stripe.StripeClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.RealComputerStore
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.http.HttpClient
import com.wasmo.http.RealHttpClient
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.payments.PaymentsService
import com.wasmo.sendemail.SendEmailService
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkEmailService
import com.wasmo.stripe.StripePaymentsService
import com.wasmo.website.RealServerHostPage
import com.wasmo.website.ServerHostPage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.Application
import io.ktor.server.engine.EmbeddedServer
import kotlin.time.Clock
import okhttp3.Call
import okhttp3.OkHttpClient

@DependencyGraph(AppScope::class)
interface AppGraph {
  val wasmoService: WasmoService
  val clientGraphFactory: ClientGraph.Factory

  @Provides
  @SingleIn(AppScope::class)
  fun provideClock(): Clock = Clock.System

  @Provides
  @SingleIn(AppScope::class)
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

  @Provides
  @SingleIn(AppScope::class)
  fun provideCookieSecret(config: WasmoServiceConfig): CookieSecret =
    CookieSecret(config.cookieSecret)

  @Provides
  @SingleIn(AppScope::class)
  fun provideDeployment(config: WasmoServiceConfig): Deployment =
    config.deployment

  @Provides
  @SingleIn(AppScope::class)
  fun provideSessionCookieSpec(config: WasmoServiceConfig): SessionCookieSpec =
    config.sessionCookieSpec

  @Provides
  @SingleIn(AppScope::class)
  fun providePostmarkCredentials(config: WasmoServiceConfig): PostmarkCredentials =
    config.postmarkCredentials

  @Provides
  @SingleIn(AppScope::class)
  fun provideStripePublishableKey(config: WasmoServiceConfig): StripePublishableKey =
    config.stripeCredentials.publishableKey

  @Provides
  @SingleIn(AppScope::class)
  fun provideObjectStore(
    config: WasmoServiceConfig,
    objectStoreFactory: ObjectStoreFactory,
  ): ObjectStore = objectStoreFactory.open(config.objectStoreAddress)

  @Provides
  @SingleIn(AppScope::class)
  fun provideStripeClient(
    config: WasmoServiceConfig,
  ): StripeClient {
    return StripeClient.StripeClientBuilder()
      .setApiKey(config.stripeCredentials.secretKey)
      .build()
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideStripePaymentsService(
    config: WasmoServiceConfig,
    stripeClient: StripeClient,
  ): StripePaymentsService = StripePaymentsService(
    deployment = config.deployment,
    sessionService = stripeClient.v1().checkout().sessions(),
    subscriptionService = stripeClient.v1().subscriptions(),
    catalog = config.catalog,
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideSendEmailService(
    factory: PostmarkEmailService.Factory,
  ): SendEmailService = factory.create()

  @Provides
  @SingleIn(AppScope::class)
  fun provideApplication(
    server: EmbeddedServer<*, *>,
  ): Application = server.application

  @Binds
  fun bindCallFactory(real: OkHttpClient): Call.Factory

  @Binds
  fun bindHttpClient(real: RealHttpClient): HttpClient

  @Binds
  fun bindRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @Binds
  fun bindServerHostPageFactory(real: RealServerHostPage.Factory): ServerHostPage.Factory

  @Binds
  fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  @Binds
  fun bindAuthenticatorDatabase(
    real: RealAuthenticatorDatabase,
  ): AuthenticatorDatabase

  @Binds
  fun bindComputerStore(
    real: RealComputerStore,
  ): ComputerStore

  @Binds
  fun bindPaymentsService(
    real: StripePaymentsService,
  ): PaymentsService

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides config: WasmoServiceConfig,
      @Provides server: EmbeddedServer<*, *>,
      @Provides wasmoDb: WasmoDb,
    ): AppGraph
  }
}
