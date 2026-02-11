package com.wasmo.ktor

import com.wasmo.computers.ComputerStore
import com.wasmo.computers.CreateComputerAction

class ActionFactory(
  val computerStore: ComputerStore,
) {
  fun createComputerAction(): CreateComputerAction {
    return CreateComputerAction(
      computerStore = computerStore,
    )
  }
}
