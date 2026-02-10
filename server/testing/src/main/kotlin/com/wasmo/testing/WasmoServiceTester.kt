package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.FileSystemObjectStore
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.WasmComputerJson
import com.wasmo.app.db.WasmoDbService
import com.wasmo.apps.InstallAppAction
import com.wasmo.apps.ObjectStoreKeyFactory
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.CreateComputerAction
import com.wasmo.computers.RealComputerStore
import java.io.Closeable
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

/**
 * Create instances with [WasmoServiceTester.start]
 */
class WasmoServiceTester private constructor(
  val service: WasmoDbService,
) : Closeable by service {
  val baseUrl = "https://example.com/".toHttpUrl()
  val clock = FakeClock()
  val fileSystem = FakeFileSystem()
  val rootObjectStore = FileSystemObjectStore(
    fileSystem = fileSystem,
    path = "/".toPath(),
  )
  val wasmoArtifactServer = WasmoArtifactServer(WasmComputerJson)
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
