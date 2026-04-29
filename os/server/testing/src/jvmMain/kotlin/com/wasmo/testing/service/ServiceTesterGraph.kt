package com.wasmo.testing.service

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.computers.AppCatalog
import com.wasmo.computers.ComputerBindings
import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.deployment.Deployment
import com.wasmo.events.EventListener
import com.wasmo.framework.ContentTypeDatabase
import com.wasmo.framework.MDN
import com.wasmo.identifiers.ForOs
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.installedapps.RealSqlService
import com.wasmo.jobs.OsJobQueue
import com.wasmo.jobs.absurd.AbsurdOsJobQueue
import com.wasmo.jobs.absurd.AbsurdService
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.payments.PaymentsService
import com.wasmo.permits.PermitService
import com.wasmo.permits.RealPermitService
import com.wasmo.sendemail.SendEmailService
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.RealSqlDatabaseFactory
import com.wasmo.sql.SqlDatabaseFactory
import com.wasmo.testing.FakeAppPublisher
import com.wasmo.testing.FakePaymentsService
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.TestDirectory
import com.wasmo.testing.apps.SampleApps
import com.wasmo.testing.call.CallTesterGraph
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.events.TestEventListener
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import wasmo.http.FakeHttpService
import wasmo.http.HttpService
import wasmo.objectstore.FakeObjectStore
import wasmo.objectstore.ObjectStore
import wasmo.sql.SqlDatabase
import wasmo.sql.SqlService
import wasmo.time.FakeClock

@DependencyGraph(
  scope = OsScope::class,
  bindingContainers = [
    ComputerBindings::class,
    InstalledAppBindings::class,
  ],
)
interface ServiceTesterGraph {
  val callTesterGraphFactory: CallTesterGraph.Factory
  val computerServiceGraphFactory: ComputerServiceGraph.Factory
  val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory

  val clientAuthenticatorFactory: ClientAuthenticator.Factory
  val clock: FakeClock
  val clientTesterFactory: ClientTester.Factory
  val deployment: Deployment
  val eventListener: TestEventListener
  val fakeHttpClient: FakeHttpService
  val fileSystem: FileSystem
  val permitService: RealPermitService

  @TestDirectory
  val testDirectory: Path
  val objectStore: FakeObjectStore
  val sendEmailService: FakeSendEmailService
  val appPublisher: FakeAppPublisher
  val wasmoDb: SqlDatabase
  val provisioningDb: SqlDatabase
  val sampleApps: SampleApps
  val absurdService: AbsurdService
  val jobQueueFactory: OsJobQueue.Factory

  @Provides
  @SingleIn(OsScope::class)
  fun provideDeployment(): Deployment = Deployment(
    baseUrl = "https://wasmo.com/".toHttpUrl(),
    sendFromEmailAddress = "noreply@wasmo.com",
  )

  @Provides
  @SingleIn(OsScope::class)
  fun provideClock(): FakeClock = FakeClock()

  @Provides
  @SingleIn(OsScope::class)
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

  @Provides
  @SingleIn(OsScope::class)
  fun provideFakeHttpClient(
    appPublisher: FakeAppPublisher,
  ): FakeHttpService = FakeHttpService().apply {
    this += appPublisher.httpHandler
  }

  @Provides
  @SingleIn(OsScope::class)
  fun provideStripePublishableKey(): StripePublishableKey =
    StripePublishableKey("pk_test_5544332211")

  @Provides
  @SingleIn(OsScope::class)
  fun provideSessionCookieSpec(): SessionCookieSpec = SessionCookieSpec.Https

  @Provides
  @SingleIn(OsScope::class)
  fun provideCookieSecret(): CookieSecret = CookieSecret("secret".encodeUtf8())

  @Provides
  @SingleIn(OsScope::class)
  fun provideAppCatalog(
    sampleApps: SampleApps,
  ): AppCatalog = sampleApps.appCatalog

  @Provides
  @SingleIn(OsScope::class)
  fun provideFakeObjectStore(): FakeObjectStore = FakeObjectStore()

  @Provides
  @SingleIn(OsScope::class)
  fun provideContentTypeDatabase(): ContentTypeDatabase = ContentTypeDatabase.MDN

  @Binds
  fun bindClock(real: FakeClock): Clock

  @Binds
  fun bindSendEmailService(real: FakeSendEmailService): SendEmailService

  @Binds
  fun bindPaymentsService(real: FakePaymentsService): PaymentsService

  @Binds
  fun bindHttpClient(real: FakeHttpService): HttpService

  @Binds
  fun bindAuthenticatorDatabase(real: RealAuthenticatorDatabase): AuthenticatorDatabase

  @Binds
  fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  @Binds
  fun bindOsJobQueueFactory(real: AbsurdOsJobQueue.Factory): OsJobQueue.Factory

  @Binds
  fun bindEventListener(real: TestEventListener): EventListener

  @Binds
  fun bindAppLoader(real: FakeAppPublisher): AppLoader

  @Binds
  @ForOs
  fun bindObjectStore(real: FakeObjectStore): ObjectStore

  @Binds
  fun bindSqlDatabaseFactory(real: RealSqlDatabaseFactory): SqlDatabaseFactory

  @Binds
  fun bindSqlService(real: RealSqlService): SqlService

  @Binds
  fun bindPermitService(real: RealPermitService): PermitService

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides wasmoDb: SqlDatabase,
      @Provides postgresqlAddress: PostgresqlAddress,
      @Provides provisioningDb: ProvisioningDb,
      @Provides coroutineScope: CoroutineScope,
      @Provides fileSystem: FileSystem,
      @Provides @TestDirectory testDirectory: Path,
    ): ServiceTesterGraph
  }
}
