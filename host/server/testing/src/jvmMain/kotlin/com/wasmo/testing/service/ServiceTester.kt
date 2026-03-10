package com.wasmo.testing.service

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.FakeClock
import com.wasmo.FakeHttpClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.app.db.WasmoDbService
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.RealAuthenticatorDatabase
import com.wasmo.testing.FakeEventListener
import com.wasmo.testing.FakePasskey
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.FakeUserAgent
import com.wasmo.testing.JobQueueTester
import com.wasmo.testing.WasmoArtifactServer
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.testing.client.ClientTester
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem

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
  val wasmoArtifactServer: WasmoArtifactServer
    get() = graph.wasmoArtifactServer
  val fakeHttpClient: FakeHttpClient
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
    wasmoArtifactServer.publish(app)
  }

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val wasmoDb = WasmoDbService.start(
      databaseName = "wasmo_test",
      user = "postgres",
      password = "password",
      hostname = "localhost",
      ssl = false,
    )
    wasmoDb.clearSchema()
    wasmoDb.migrate()

    val serviceTesterGraphFactory = createGraphFactory<ServiceTesterGraph.Factory>()

    try {
      coroutineScope {
        this@ServiceTester.graph = serviceTesterGraphFactory.create(
          wasmoDbService = wasmoDb,
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
