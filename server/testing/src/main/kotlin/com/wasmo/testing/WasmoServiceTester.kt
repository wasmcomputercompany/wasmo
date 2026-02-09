package com.wasmo.testing

import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.testing.FakeClock
import com.wasmo.computers.CreateComputerAction
import java.io.Closeable

/**
 * Create instances with [WasmoServiceTester.start]
 */
class WasmoServiceTester private constructor(
  val service: WasmoDbService,
) : Closeable by service {
  val clock = FakeClock()

  fun createComputerAction() = CreateComputerAction(
    clock = clock,
    service = service,
  )

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
