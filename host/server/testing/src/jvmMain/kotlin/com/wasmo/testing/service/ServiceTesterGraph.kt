package com.wasmo.testing.service

import com.wasmo.FakeClock
import com.wasmo.FakeHttpClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.AppCatalog
import com.wasmo.computers.ComputerBindings
import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.events.EventListener
import com.wasmo.identifiers.ForHost
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.jobs.JobQueue
import com.wasmo.jobs.JobQueueEventListener
import com.wasmo.jobs.MemoryJobQueue
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.objectstore.filesystem.FileSystemObjectStoreBindings
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.payments.PaymentsService
import com.wasmo.sendemail.SendEmailService
import com.wasmo.testing.FakeEventListener
import com.wasmo.testing.FakePaymentsService
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.JobQueueTester
import com.wasmo.testing.WasmoArtifactServer
import com.wasmo.testing.apps.TestAppCatalog
import com.wasmo.testing.call.CallTesterGraph
import dev.zacsweers.metro.AppScope
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
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import wasmo.http.HttpClient
import wasmo.objectstore.ObjectStore

@DependencyGraph(
  scope = AppScope::class,
  bindingContainers = [
    ComputerBindings::class,
    InstalledAppBindings::class,
    FileSystemObjectStoreBindings::class,
  ],
)
interface ServiceTesterGraph {
  val callTesterGraphFactory: CallTesterGraph.Factory
  val computerServiceGraphFactory: ComputerServiceGraph.Factory
  val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory

  val clientAuthenticatorFactory: ClientAuthenticator.Factory
  val clock: FakeClock
  val deployment: Deployment
  val eventListener: FakeEventListener
  val fakeHttpClient: FakeHttpClient
  val fileSystem: FakeFileSystem
  val jobQueueTester: JobQueueTester
  val sendEmailService: FakeSendEmailService
  val wasmoArtifactServer: WasmoArtifactServer
  val wasmoDb: WasmoDbService
  val paymentsService: FakePaymentsService

  @Provides
  @SingleIn(AppScope::class)
  fun provideDeployment(): Deployment = Deployment(
    baseUrl = "https://wasmo.com/".toHttpUrl(),
    sendFromEmailAddress = "noreply@wasmo.com",
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideClock(): FakeClock = FakeClock()

  @Provides
  @SingleIn(AppScope::class)
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

  @Provides
  @SingleIn(AppScope::class)
  fun provideFakeHttpClient(
    wasmoArtifactServer: WasmoArtifactServer,
  ): FakeHttpClient = FakeHttpClient().apply {
    this += wasmoArtifactServer
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideStripePublishableKey(): StripePublishableKey =
    StripePublishableKey("pk_test_5544332211")

  @Provides
  @ForHost
  @SingleIn(AppScope::class)
  fun provideObjectStore(
    objectStoreFactory: ObjectStoreFactory,
    fileSystem: FakeFileSystem,
  ): ObjectStore = objectStoreFactory.open(
    FileSystemObjectStoreAddress(
      fileSystem = fileSystem,
      path = "/".toPath(),
    ),
  )

  @Provides
  @SingleIn(AppScope::class)
  fun provideFileSystem(): FakeFileSystem = FakeFileSystem()

  @Provides
  @SingleIn(AppScope::class)
  fun provideSessionCookieSpec(): SessionCookieSpec = SessionCookieSpec.Https

  @Provides
  @SingleIn(AppScope::class)
  fun provideCookieSecret(): CookieSecret = CookieSecret("secret".encodeUtf8())

  @Provides
  @SingleIn(AppScope::class)
  fun provideAppCatalog(): AppCatalog = TestAppCatalog

  @Binds
  fun provideJobQueueEventListener(real: JobQueueTester): JobQueueEventListener

  @Binds
  fun bindClock(real: FakeClock): Clock

  @Binds
  fun bindFileSystem(real: FakeFileSystem): FileSystem

  @Binds
  fun bindWasmoDb(real: WasmoDbService): WasmoDb

  @Binds
  fun bindSendEmailService(real: FakeSendEmailService): SendEmailService

  @Binds
  fun bindPaymentsService(real: FakePaymentsService): PaymentsService

  @Binds
  fun bindHttpClient(real: FakeHttpClient): HttpClient

  @Binds
  fun bindAuthenticatorDatabase(real: RealAuthenticatorDatabase): AuthenticatorDatabase

  @Binds
  fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  @Binds
  fun bindInstallAppJobQueue(real: MemoryJobQueue<InstallAppJob>): JobQueue<InstallAppJob>

  @Binds
  fun bindEventListener(real: FakeEventListener): EventListener

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides wasmoDbService: WasmoDbService,
      @Provides coroutineScope: CoroutineScope,
    ): ServiceTesterGraph
  }
}
