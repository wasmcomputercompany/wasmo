package com.wasmo.testing.service

import com.wasmo.accounts.AccountsBindings
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.computers.ComputerBindings
import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.jobs.OsJobQueue
import com.wasmo.jobs.absurd.AbsurdBindings
import com.wasmo.jobs.absurd.AbsurdService
import com.wasmo.passkeys.PasskeysBindings
import com.wasmo.permits.PermitService
import com.wasmo.permits.PermitsBindings
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.SqlServiceBindings
import com.wasmo.testing.FakeAppPublisher
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.TestDirectory
import com.wasmo.testing.apps.SampleApps
import com.wasmo.testing.call.CallTesterGraph
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.events.TestEventListener
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import okio.FileSystem
import okio.Path
import wasmo.http.FakeHttpService
import wasmo.objectstore.FakeObjectStore
import wasmo.sql.SqlDatabase
import wasmo.time.FakeClock

@DependencyGraph(
  scope = OsScope::class,
  bindingContainers = [
    AbsurdBindings::class,
    AccountsBindings::class,
    ComputerBindings::class,
    InstalledAppBindings::class,
    PasskeysBindings::class,
    PermitsBindings::class,
    SqlServiceBindings::class,
    TestServiceBindings::class,
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
  val permitService: PermitService

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
