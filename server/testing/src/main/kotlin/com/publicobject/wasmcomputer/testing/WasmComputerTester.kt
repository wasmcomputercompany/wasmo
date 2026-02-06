package com.publicobject.wasmcomputer.testing

import com.publicobject.wasmcomputer.app.db.WasmComputerDbService
import com.publicobject.wasmcomputer.common.testing.FakeClock
import java.io.Closeable

/**
 * Create instances with [WasmComputerTester.start]
 */
class WasmComputerTester private constructor(
  val service: WasmComputerDbService,
) : Closeable by service {
  val clock = FakeClock()

  companion object {
    fun start(): WasmComputerTester {
      val service = WasmComputerDbService.start(
        databaseName = "wasmcomputer_test",
        user = "postgres",
        password = "password",
        hostname = "localhost",
        ssl = false,
      )
      service.clearSchema()
      service.migrate()
      return WasmComputerTester(service)
    }
  }
}
