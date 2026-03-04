package com.wasmo.testing

import com.wasmo.FakeClock
import com.wasmo.FakeHttpClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.InstallAppJob
import com.wasmo.computers.InstallAppJobExecutor
import com.wasmo.computers.RealComputerStore
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.http.HttpClient
import com.wasmo.jobs.JobExecutor
import com.wasmo.jobs.JobQueue
import com.wasmo.jobs.JobQueueEventListener
import com.wasmo.jobs.MemoryJobQueue
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.AuthenticatorDatabase
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.payments.PaymentsService
import com.wasmo.sendemail.SendEmailService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.KSerializer
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

@DependencyGraph(AppScope::class)
interface ServiceTesterGraph {
  val serviceTester: ServiceTester
  val clientTesterGraphFactory: ClientTesterGraph.Factory

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
  fun provideCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Default)

  @Provides
  @SingleIn(AppScope::class)
  fun provideInstallAppJob(): KSerializer<InstallAppJob> = InstallAppJob.serializer()

  @Binds
  fun provideJobQueueEventListener(real: JobQueueTester): JobQueueEventListener

  @Binds
  fun bindComputerStore(real: RealComputerStore): ComputerStore

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
  fun bind(real: RealClientAuthenticator.Factory): ClientAuthenticator.Factory

  @Binds
  fun bind(real: InstallAppJobExecutor): JobExecutor<InstallAppJob>

  @Binds
  fun bind(real: MemoryJobQueue<InstallAppJob>): JobQueue<InstallAppJob>

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides wasmoDbService: WasmoDbService,
    ): ServiceTesterGraph
  }
}
