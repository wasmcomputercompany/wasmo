package com.wasmo.testing

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.FakeClock
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.app.db.WasmoDbService
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.RealAuthenticatorDatabase
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem

/**
 * Create instances with [ServiceTester.start]
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
  val wasmoArtifactServer: WasmoArtifactServer
    get() = graph.wasmoArtifactServer
  val jobQueueTester: JobQueueTester
    get() = graph.jobQueueTester
  val eventListener: FakeEventListener
    get() = graph.eventListener
  val baseUrl: HttpUrl
    get() = deployment.baseUrl

  val origin: String
    get() = baseUrl.toString()

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    val clientTesterGraph = graph.clientTesterGraphFactory.create(
      clientAuthenticator = clientAuthenticator,
      sessionCookie = sessionCookie,
    )
    return clientTesterGraph.clientTester
  }

  fun newPasskey() = FakePasskey(
    rpId = baseUrl.host,
    id = "passkey-${nextPasskeyId++}".encodeUtf8().base64Url(),
    aaguid = RealAuthenticatorDatabase.ApplePasswords,
  )

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
