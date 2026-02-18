package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.WasmoJson
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.InstallAppAction
import com.wasmo.computers.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import java.io.Closeable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

/**
 * Create instances with [WasmoServiceTester.start]
 */
class WasmoServiceTester private constructor(
  val service: WasmoDbService,
) : Closeable by service {
  val baseUrl = "https://wasmo.com/".toHttpUrl()
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
  val wasmoArtifactServer = WasmoArtifactServer(
    json = WasmoJson,
  )
  val httpClient = FakeHttpClient().apply {
    this += wasmoArtifactServer
  }
  val objectStoreKeyFactory = ObjectStoreKeyFactory()
  val computerStore = RealComputerStore(
    baseUrl = baseUrl,
    clock = clock,
    rootObjectStore = rootObjectStore,
    httpClient = httpClient,
    objectStoreKeyFactory = objectStoreKeyFactory,
    service = service,
  )

  fun createComputerAction() = CreateComputerAction(
    computerStore = computerStore,
  )

  fun installAppAction() = InstallAppAction(
    computerStore = computerStore,
  )

  fun createComputer(slug: String): ComputerTester {
    createComputerAction().createComputer(
      request = CreateComputerRequest(
        slug = slug,
      ),
    )

    return ComputerTester(
      slug = slug,
    )
  }

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
