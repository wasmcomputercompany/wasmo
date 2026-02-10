package com.wasmo.testing

import com.wasmo.FakeHttpClient
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.WasmComputerJson
import com.wasmo.app.db.WasmoDbService
import com.wasmo.apps.AppLoader
import com.wasmo.apps.InstallAppAction
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.CreateComputerAction
import java.io.Closeable
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Create instances with [WasmoServiceTester.start]
 */
class WasmoServiceTester private constructor(
  val service: WasmoDbService,
) : Closeable by service {
  val clock = FakeClock()
  val baseUrl = "https://example.com/".toHttpUrl()
  val wasmoArtifactServer = WasmoArtifactServer(WasmComputerJson)
  val httpClient = FakeHttpClient().apply {
    this += wasmoArtifactServer
  }
  val appLoader = AppLoader(
    json = WasmComputerJson,
    httpClient = httpClient,
  )

  fun createComputerAction() = CreateComputerAction(
    clock = clock,
    service = service,
  )

  fun installAppAction() = InstallAppAction(
    clock = clock,
    appLoader = appLoader,
    service = service,
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
