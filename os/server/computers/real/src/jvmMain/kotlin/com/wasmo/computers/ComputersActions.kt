package com.wasmo.computers

import com.wasmo.framework.UserAgent

interface ComputersActions {
  val createComputerSpecRpc: CreateComputerSpecRpc

  interface Factory {
    fun create(userAgent: UserAgent): ComputersActions
  }
}
