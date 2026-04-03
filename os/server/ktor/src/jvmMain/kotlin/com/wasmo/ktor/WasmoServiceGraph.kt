package com.wasmo.ktor

import com.stripe.StripeClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.computers.AppCatalog
import com.wasmo.computers.ComputerBindings
import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.computers.loadDefaultAppCatalogFromResources
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.events.EventListener
import com.wasmo.events.LoggingEventListener
import com.wasmo.framework.ContentTypeDatabase
import com.wasmo.framework.MDN
import com.wasmo.http.OkHttpClientHttpService
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ForOs
import com.wasmo.identifiers.HandlerId
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.ApplicationJob
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.installedapps.RealApplicationJobHandler
import com.wasmo.jobs.JobQueueEventListener
import com.wasmo.jobs.JobStore
import com.wasmo.jobs.MemoryJobStore
import com.wasmo.journal.server.JournalWasmoApp
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.objectstore.filesystem.FileSystemObjectStoreBindings
import com.wasmo.objectstore.s3.S3ObjectStoreBindings
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.payments.PaymentsService
import com.wasmo.sendemail.SendEmailService
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkEmailService
import com.wasmo.stripe.StripePaymentsService
import com.wasmo.wasm.AppLoader
import com.wasmo.wasm.JvmAppLoader
import com.wasmo.website.RealServerHostPage
import com.wasmo.website.ServerHostPage
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.Application
import io.ktor.server.engine.EmbeddedServer
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Call
import okhttp3.Dns
import okhttp3.OkHttpClient
import okio.FileSystem
import wasmo.app.WasmoApp
import wasmo.http.HttpService
import wasmo.objectstore.ObjectStore
import wasmo.sql.SqlService

@DependencyGraph(
  scope = OsScope::class,
  bindingContainers = [
    ComputerBindings::class,
    FileSystemObjectStoreBindings::class,
    InstalledAppBindings::class,
    S3ObjectStoreBindings::class,
  ],
)
internal interface WasmoServiceGraph {
  val wasmoService: WasmoService
  val callGraphFactory: CallGraph.Factory
  val computerServiceGraphFactory: ComputerServiceGraph.Factory
  val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory

  @Provides
  @SingleIn(OsScope::class)
  fun provideClock(): Clock = Clock.System

  @Provides
  @SingleIn(OsScope::class)
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .dns(LocalhostSubdomainsDns(Dns.SYSTEM))
    .build()

  @Provides
  @SingleIn(OsScope::class)
  fun provideCookieSecret(config: WasmoService.Config): CookieSecret =
    CookieSecret(config.cookieSecret)

  @Provides
  @SingleIn(OsScope::class)
  fun provideDeployment(config: WasmoService.Config): Deployment =
    config.deployment

  @Provides
  @SingleIn(OsScope::class)
  fun provideSessionCookieSpec(config: WasmoService.Config): SessionCookieSpec =
    config.sessionCookieSpec

  @Provides
  @SingleIn(OsScope::class)
  fun providePostmarkCredentials(config: WasmoService.Config): PostmarkCredentials =
    config.postmarkCredentials

  @Provides
  @SingleIn(OsScope::class)
  fun provideStripePublishableKey(config: WasmoService.Config): StripePublishableKey =
    config.stripeCredentials.publishableKey

  @Provides
  @ForOs
  @SingleIn(OsScope::class)
  fun provideObjectStore(
    config: WasmoService.Config,
    objectStoreFactory: ObjectStoreFactory,
  ): ObjectStore = objectStoreFactory.open(config.objectStoreAddress)

  @Provides
  @SingleIn(OsScope::class)
  fun provideStripeClient(
    config: WasmoService.Config,
  ): StripeClient {
    return StripeClient.StripeClientBuilder()
      .setApiKey(config.stripeCredentials.secretKey)
      .build()
  }

  @Provides
  @SingleIn(OsScope::class)
  fun provideStripePaymentsService(
    config: WasmoService.Config,
    stripeClient: StripeClient,
  ): StripePaymentsService = StripePaymentsService(
    deployment = config.deployment,
    sessionService = stripeClient.v1().checkout().sessions(),
    subscriptionService = stripeClient.v1().subscriptions(),
    catalog = config.catalog,
  )

  @Provides
  @SingleIn(OsScope::class)
  fun provideSendEmailService(
    factory: PostmarkEmailService.Factory,
  ): SendEmailService = factory.create()

  @Provides
  @SingleIn(OsScope::class)
  fun provideApplication(
    server: EmbeddedServer<*, *>,
  ): Application = server.application

  @Provides
  @SingleIn(OsScope::class)
  fun provideJobQueueEventListener(): JobQueueEventListener = JobQueueEventListener.None

  @Provides
  @SingleIn(OsScope::class)
  fun provideCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Default)

  @Provides
  @SingleIn(OsScope::class)
  fun provideAppCatalog(): AppCatalog = loadDefaultAppCatalogFromResources()

  @Provides
  @SingleIn(OsScope::class)
  fun provideFileSystem(): FileSystem = FileSystem.SYSTEM

  @Provides
  @SingleIn(OsScope::class)
  fun provideWasmoAppFactories(): Map<AppSlug, WasmoApp.Factory> = mapOf(
    AppSlug("journal") to JournalWasmoApp.Factory(),
  )

  @Provides
  @SingleIn(OsScope::class)
  fun provideContentTypeDatabase(): ContentTypeDatabase = ContentTypeDatabase.MDN

  @Provides
  @SingleIn(OsScope::class)
  fun bindJobHandlerMap(
    applicationJobHandler: JobStore.Handler<ApplicationJob>,
    installAppJobHandler: JobStore.Handler<InstallAppJob>,
  ): Map<HandlerId<*>, JobStore.Handler<*>> = mapOf(
    ApplicationJob.HandlerId to applicationJobHandler,
    InstallAppJob.HandlerId to installAppJobHandler,
  )

  @Binds
  fun bindApplicationJobHandler(real: RealApplicationJobHandler): JobStore.Handler<ApplicationJob>

  @Binds
  fun bind(real: MemoryJobStore): JobStore

  @Binds
  fun bindCallFactory(real: OkHttpClient): Call.Factory

  @Binds
  fun bindHttpClient(real: OkHttpClientHttpService): HttpService

  @Binds
  fun bindRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @Binds
  fun bindServerHostPageFactory(real: RealServerHostPage.Factory): ServerHostPage.Factory

  @Binds
  fun bindEventListener(real: LoggingEventListener): EventListener

  @Binds
  fun bindLogger(real: KtorLogger): Logger

  @Binds
  fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  @Binds
  fun bindAuthenticatorDatabase(
    real: RealAuthenticatorDatabase,
  ): AuthenticatorDatabase

  @Binds
  fun bindPaymentsService(
    real: StripePaymentsService,
  ): PaymentsService

  @Binds
  fun bindAppLoader(real: JvmAppLoader): AppLoader

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides config: WasmoService.Config,
      @Provides server: EmbeddedServer<*, *>,
      @Provides wasmoDb: WasmoDb,
      @Provides sqlService: SqlService,
    ): WasmoServiceGraph
  }
}
