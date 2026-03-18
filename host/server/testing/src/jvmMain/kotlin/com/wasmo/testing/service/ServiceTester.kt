package com.wasmo.testing.service

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.app.db.WasmoDbService
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.sql.jdbc.connectPostgresql
import com.wasmo.sql.r2dbc.asSqlService
import com.wasmo.sql.r2dbc.connectPostgresqlAsync
import com.wasmo.testing.FakeAppPublisher
import com.wasmo.testing.FakeEventListener
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.FakeUserAgent
import com.wasmo.testing.JobQueueTester
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.client.ClientTester
import com.wasmo.testing.sql.TestDatabaseAddress
import com.wasmo.testing.sql.clearSchema
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem
import wasmo.http.FakeHttpService
import wasmo.time.FakeClock

/**
 * Use this with Burst and [app.cash.burst.InterceptTest].
 */
class ServiceTester : CoroutineTestInterceptor {
  private lateinit var graph: ServiceTesterGraph

  val wasmoDb: WasmoDbService
    get() = graph.wasmoDb
  val deployment: Deployment
    get() = graph.deployment
  val clock: FakeClock
    get() = graph.clock
  val fileSystem: FakeFileSystem
    get() = graph.fileSystem
  val clientAuthenticatorFactory: ClientAuthenticator.Factory
    get() = graph.clientAuthenticatorFactory
  val sendEmailService: FakeSendEmailService
    get() = graph.sendEmailService
  val jobQueueTester: JobQueueTester
    get() = graph.jobQueueTester
  val eventListener: FakeEventListener
    get() = graph.eventListener
  val appPublisher: FakeAppPublisher
    get() = graph.appPublisher
  val fakeHttpClient: FakeHttpService
    get() = graph.fakeHttpClient
  val baseUrl: HttpUrl
    get() = deployment.baseUrl

  val origin: String
    get() = baseUrl.toString()

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    return ClientTester(
      deployment = deployment,
      clientAuthenticator = clientAuthenticator,
      callTesterGraphFactory = graph.callTesterGraphFactory,
      sessionCookie = sessionCookie,
      jobQueueTester = jobQueueTester,
      eventListener = eventListener,
      paymentsService = graph.paymentsService,
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
    val dataSource = connectPostgresql(TestDatabaseAddress)
    dataSource.clearSchema()

    val wasmoDb = WasmoDbService(
      dataSource = dataSource,
      jdbcDriver = dataSource.asJdbcDriver(),
    )
    wasmoDb.migrate()

    val sqlService = connectPostgresqlAsync(TestDatabaseAddress)
      .asSqlService()

    val serviceTesterGraphFactory = createGraphFactory<ServiceTesterGraph.Factory>()

    try {
      coroutineScope {
        this@ServiceTester.graph = serviceTesterGraphFactory.create(
          wasmoDbService = wasmoDb,
          sqlService = sqlService,
          coroutineScope = this,
        )
        testFunction()
      }
    } finally {
      wasmoDb.close()
      fileSystem.checkNoOpenFiles()
      fileSystem.close()
    }
  }
}
