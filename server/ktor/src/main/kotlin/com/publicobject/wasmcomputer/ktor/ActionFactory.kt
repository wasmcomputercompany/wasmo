package com.publicobject.wasmcomputer.ktor

import com.publicobject.wasmcomputer.app.db.WasmComputerDbService
import com.publicobject.wasmcomputer.computer.actions.CreateComputerAction
import kotlin.time.Clock

class ActionFactory(
  val clock: Clock,
  val service: WasmComputerDbService,
) {
  fun createComputerAction(): CreateComputerAction {
    return CreateComputerAction(
      clock = clock,
      service = service,
    )
  }
}
