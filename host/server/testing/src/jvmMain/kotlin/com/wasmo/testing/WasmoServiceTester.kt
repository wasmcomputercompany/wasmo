package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.HmacChallenger
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookie
import com.wasmo.accounts.SessionCookieEncoder
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.WasmoJson
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.computers.SubscriptionUpdater
import com.wasmo.deployment.Deployment
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.RealAuthenticatorDatabase
import java.io.Closeable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

/**
 * Create instances with [WasmoServiceTester.start]
 */
class WasmoServiceTester private constructor(
  val wasmoDbService: WasmoDbService,
) : Closeable by wasmoDbService {
  val deployment = Deployment(
    baseUrl = "https://wasmo.com/".toHttpUrl(),
    sendFromEmailAddress = "noreply@wasmo.com",
  )
  val baseUrl get() = deployment.baseUrl
  val origin: String
    get() = baseUrl.toString()
  val clock = FakeClock()
  val fileSystem = FakeFileSystem()
  val paymentsService = FakePaymentsService(
    clock = clock,
  )
  val objectStoreFactory = ObjectStoreFactory(
    clock = clock,
    client = OkHttpClient(),
  )
  val rootObjectStore = objectStoreFactory.open(
    FileSystemObjectStoreAddress(
      fileSystem = fileSystem,
      path = "/".toPath(),
    ),
  )

  val challengerFactory = object : HmacChallenger.Factory {
    override fun create(cookieToken: String) = HmacChallenger(
      clock = clock,
      cookieSecret = CookieSecret("secret".encodeUtf8()),
      cookieToken = cookieToken,
    )
  }

  val cookieClientFactory = object : CookieClient.Factory {
    override fun create(
      sessionCookie: SessionCookie,
      userAgent: String?,
      ip: String?,
    ) = CookieClient(
      clock = clock,
      wasmoDbService = wasmoDbService,
      hmacChallengerFactory = challengerFactory,
      sessionCookie = sessionCookie,
      userAgent = userAgent,
      ip = ip,
    )
  }

  val clientAuthenticatorFactory = object : RealClientAuthenticator.Factory {
    override fun create(userAgent: ClientAuthenticator.UserAgent) = RealClientAuthenticator(
      clock = clock,
      deployment = deployment,
      sessionCookieSpec = SessionCookieSpec.Https,
      sessionCookieEncoder = SessionCookieEncoder(
        secret = CookieSecret("password".encodeUtf8()),
      ),
      cookieClientFactory = cookieClientFactory,
      userAgent = userAgent,
    )
  }

  val sendEmailService = FakeSendEmailService()

  val objectStoreKeyFactory = ObjectStoreKeyFactory()

  val wasmoArtifactServer = WasmoArtifactServer(
    json = WasmoJson,
  )
  val httpClient = FakeHttpClient().apply {
    this += wasmoArtifactServer
  }

  val computerStore = RealComputerStore(
    deployment = deployment,
    clock = clock,
    rootObjectStore = rootObjectStore,
    httpClient = httpClient,
    objectStoreKeyFactory = objectStoreKeyFactory,
    wasmoDbService = wasmoDbService,
  )

  val computerSpecStore = ComputerSpecStore(
    clock = clock,
    wasmoDbService = wasmoDbService,
  )

  val subscriptionUpdater = SubscriptionUpdater(
    clock = clock,
    wasmoDbService = wasmoDbService,
    paymentsService = paymentsService,
    computerSpecStore = computerSpecStore,
  )

  val stripePublishableKey = StripePublishableKey("pk_test_5544332211")

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    return ClientTester(
      clock = clock,
      wasmoDbService = wasmoDbService,
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
    wasmoDbService.close()
    fileSystem.checkNoOpenFiles()
    fileSystem.close()
  }

  companion object {
    fun start(): WasmoServiceTester {
      val service = WasmoDbService.start(
        databaseName = "wasmo_test",
        user = "postgres",
        password = "password",
        hostname = "localhost",
        ssl = false,
      )
      service.clearSchema()
      service.migrate()
      return WasmoServiceTester(service)
    }
  }
}
