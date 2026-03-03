package com.wasmo.testing

import com.wasmo.FakeClock
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.app.db.WasmoDbService
import com.wasmo.deployment.Deployment
import com.wasmo.passkeys.RealAuthenticatorDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import java.io.Closeable
import okhttp3.HttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem

/**
 * Create instances with [WasmoServiceTester.start]
 */
@Inject
@SingleIn(AppScope::class)
class WasmoServiceTester private constructor(
  val wasmoDb: WasmoDbService,
  val deployment: Deployment,
  val clock: FakeClock,
  val fileSystem: FakeFileSystem,
  val clientAuthenticatorFactory: RealClientAuthenticator.Factory,
  val sendEmailService: FakeSendEmailService,
  val wasmoArtifactServer: WasmoArtifactServer,
  val clientGraphFactory: ClientTesterGraph.Factory,
) : Closeable by wasmoDb {
  val baseUrl: HttpUrl
    get() = deployment.baseUrl
  val origin: String
    get() = baseUrl.toString()

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    val clientTesterGraph = clientGraphFactory.create(
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

  override fun close() {
    wasmoDb.close()
    fileSystem.checkNoOpenFiles()
    fileSystem.close()
  }

  companion object {
    fun start(): WasmoServiceTester {
      val wasmoDbService = WasmoDbService.start(
        databaseName = "wasmo_test",
        user = "postgres",
        password = "password",
        hostname = "localhost",
        ssl = false,
      )
      wasmoDbService.clearSchema()
      wasmoDbService.migrate()

      val wasmoServiceTesterGraphFactory = createGraphFactory<WasmoServiceTesterGraph.Factory>()
      val serviceTesterGraph = wasmoServiceTesterGraphFactory.create(
        wasmoDbService = wasmoDbService,
      )
      return serviceTesterGraph.wasmoServiceTester
    }
  }
}
