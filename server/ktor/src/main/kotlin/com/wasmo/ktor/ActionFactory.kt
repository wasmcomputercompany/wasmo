package com.wasmo.ktor

import com.wasmo.app.db.WasmoDbService
import com.wasmo.computers.CreateComputerAction
import kotlin.time.Clock

class ActionFactory(
  val clock: Clock,
  val service: WasmoDbService,
) {
  fun createComputerAction(): CreateComputerAction {
    return CreateComputerAction(
      clock = clock,
      service = service,
    )
  }
}
