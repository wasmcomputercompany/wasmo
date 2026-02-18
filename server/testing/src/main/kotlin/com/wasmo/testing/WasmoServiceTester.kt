package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.accounts.CookieClient
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieEncoder
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.WasmoJson
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.passkeys.HmacChallenger
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
  val service: WasmoDbService,
) : Closeable by service {
  val baseUrl = "https://wasmo.com/".toHttpUrl()
  val origin: String
    get() = baseUrl.toString()
  val clock = FakeClock()
  val fileSystem = FakeFileSystem()
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

  val challengerFactory = HmacChallenger.Factory(clock, "secret".encodeUtf8())

  val clientAuthenticatorFactory = RealClientAuthenticator.Factory(
    clock = clock,
    sessionCookieSpec = SessionCookieSpec.Https,
    sessionCookieEncoder = SessionCookieEncoder(
      secret = "password".encodeUtf8(),
    ),
    cookieClientFactory = CookieClient.Factory(
      clock = clock,
      cookieQueries = service.cookieQueries,
      accountQueries = service.accountQueries,
    ),
  )

  val objectStoreKeyFactory = ObjectStoreKeyFactory()

  val wasmoArtifactServer = WasmoArtifactServer(
    json = WasmoJson,
  )
  val httpClient = FakeHttpClient().apply {
    this += wasmoArtifactServer
  }

  val computerStore = RealComputerStore(
    baseUrl = baseUrl,
    clock = clock,
    rootObjectStore = rootObjectStore,
    httpClient = httpClient,
    objectStoreKeyFactory = objectStoreKeyFactory,
    service = service,
  )

  private var nextPasskeyId = 1

  fun newClient(): ClientTester {
    val userAgent = FakeUserAgent()
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    val sessionCookie = clientAuthenticator.updateSessionCookie()
    return ClientTester(
      clock = clock,
      service = service,
      clientAuthenticator = clientAuthenticator,
      computerStore = computerStore,
      baseUrl = baseUrl,
      challenger = challengerFactory.create(sessionCookie.token),
    )
  }

  fun newPasskey() = FakePasskey(
    rpId = baseUrl.host,
    id = "passkey-${nextPasskeyId++}".encodeUtf8().base64Url(),
    aaguid = RealAuthenticatorDatabase.ApplePasswords,
  )

  override fun close() {
    service.close()
    fileSystem.checkNoOpenFiles()
    fileSystem.close()
  }

  companion object {
    fun start(): WasmoServiceTester {
      val service = WasmoDbService.start(
        databaseName = "wasmcomputer_test",
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
