package com.wasmo.testing.service

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.db.migrate
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.sql.PostgresqlClient
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.asSqlDatabase
import com.wasmo.sql.testing.TestDatabaseAddress
import com.wasmo.sql.testing.clearSchema
import com.wasmo.sql.withConnection
import com.wasmo.support.tokens.newToken
import com.wasmo.testing.FakeAppPublisher
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.FakeUserAgent
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.apps.SampleApps
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.events.TestEventListener
import dev.zacsweers.metro.createGraphFactory
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import wasmo.http.FakeHttpService
import wasmo.objectstore.FakeObjectStore
import wasmo.time.FakeClock

/**
 * Use this with Burst and [app.cash.burst.InterceptTest].
 */
class ServiceTester : CoroutineTestInterceptor {
  private lateinit var graph: ServiceTesterGraph

  private val clientAuthenticatorFactory: ClientAuthenticator.Factory
    get() = graph.clientAuthenticatorFactory
  private val clientTesterFactory: ClientTester.Factory
    get() = graph.clientTesterFactory
  private val baseUrl: HttpUrl
    get() = graph.deployment.baseUrl
  private val appPublisher: FakeAppPublisher
    get() = graph.appPublisher

  val clock: FakeClock
    get() = graph.clock
  val objectStore: FakeObjectStore
    get() = graph.objectStore
  val sendEmailService: FakeSendEmailService
    get() = graph.sendEmailService
  val fakeHttpClient: FakeHttpService
    get() = graph.fakeHttpClient
  val fileSystem: FileSystem
    get() = graph.fileSystem
  val testDirectory: Path
    get() = graph.testDirectory
  val sampleApps: SampleApps
    get() = graph.sampleApps
  val eventListener: TestEventListener
    get() = graph.eventListener

  val origin: String
    get() = baseUrl.toString()

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    return clientTesterFactory.create(
      clientAuthenticator = clientAuthenticator,
      sessionCookie = sessionCookie,
    )
  }

  fun newPasskey() = FakePasskey(
    rpId = baseUrl.host,
    id = "passkey-${nextPasskeyId++}".encodeUtf8().base64Url(),
    aaguid = RealAuthenticatorDatabase.ApplePasswords,
  )

  fun publishApp(app: PublishedApp) {
    appPublisher.publish(app)
  }

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val fileSystem = FileSystem.SYSTEM
    val testDirectory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / testFunction.toString() / newToken()
    fileSystem.createDirectories(testDirectory)

    val postgresqlClient = PostgresqlClient(TestDatabaseAddress)
    postgresqlClient.withConnection {
      it.clearSchema()
    }

    val wasmoDb = postgresqlClient.asSqlDatabase()
    val provisioningDb = ProvisioningDb(
      address = TestDatabaseAddress,
      provisioningDb = PostgresqlClient(TestDatabaseAddress).asSqlDatabase(),
    )

    wasmoDb.withConnection {
      migrate()
    }

    // Use a custom, non-test dispatcher because the PostgreSQL dispatcher client suspends
    // waiting on I/O, and the test dispatchers don't like that.
    withContext(Dispatchers.Default) {
      withTimeout(5.seconds) {
        val serviceTesterGraphFactory = createGraphFactory<ServiceTesterGraph.Factory>()
        coroutineScope {
          this@ServiceTester.graph = serviceTesterGraphFactory.create(
            wasmoDb = wasmoDb,
            provisioningDb = provisioningDb,
            coroutineScope = this,
            fileSystem = fileSystem,
            testDirectory = testDirectory,
          )
          testFunction()
        }
      }
    }
  }
}
