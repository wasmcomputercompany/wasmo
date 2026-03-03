package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.HmacChallenger
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.computers.SubscriptionUpdater
import com.wasmo.deployment.Deployment
import com.wasmo.objectstore.ObjectStore
import com.wasmo.objectstore.ObjectStoreFactory
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
  val paymentsService: FakePaymentsService,
  val objectStoreFactory: ObjectStoreFactory,
  val rootObjectStore: ObjectStore,
  val challengerFactory: HmacChallenger.Factory,
  val cookieClientFactory: CookieClient.Factory,
  val clientAuthenticatorFactory: RealClientAuthenticator.Factory,
  val sendEmailService: FakeSendEmailService,
  val objectStoreKeyFactory: ObjectStoreKeyFactory,
  val wasmoArtifactServer: WasmoArtifactServer,
  val httpClient: FakeHttpClient,
  val stripePublishableKey: StripePublishableKey,
) : Closeable by wasmoDb {
  val baseUrl: HttpUrl
    get() = deployment.baseUrl
  val origin: String
    get() = baseUrl.toString()

  val computerStore = RealComputerStore(
    deployment = deployment,
    clock = clock,
    rootObjectStore = rootObjectStore,
    httpClient = httpClient,
    objectStoreKeyFactory = objectStoreKeyFactory,
    wasmoDb = wasmoDb,
  )

  val computerSpecStore = ComputerSpecStore(
    clock = clock,
    wasmoDb = wasmoDb,
  )

  val subscriptionUpdater = SubscriptionUpdater(
    clock = clock,
    wasmoDb = wasmoDb,
    paymentsService = paymentsService,
    computerSpecStore = computerSpecStore,
  )

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    return ClientTester(
      clock = clock,
      wasmoDb = wasmoDb,
      deployment = deployment,
      sendEmailService = sendEmailService,
      clientAuthenticator = clientAuthenticator,
      computerStore = computerStore,
      computerSpecStore = computerSpecStore,
      subscriptionUpdater = subscriptionUpdater,
      stripePublishableKey = stripePublishableKey,
      paymentsService = paymentsService,
      challenger = challengerFactory.create(sessionCookie.token),
    )
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

      val wasmoServiceGraphFactory = createGraphFactory<WasmoServiceTesterGraph.Factory>()
      val serviceGraph = wasmoServiceGraphFactory.create(
        wasmoDbService = wasmoDbService,
      )
      return serviceGraph.wasmoServiceTester
    }
  }
}
