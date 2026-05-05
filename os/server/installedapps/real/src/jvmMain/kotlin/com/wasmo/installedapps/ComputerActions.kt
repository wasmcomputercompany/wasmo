package com.wasmo.installedapps

import com.wasmo.framework.UserAgent

interface ComputerActions {
  val installAppRpc: InstallAppRpc

  interface Factory {
    fun create(userAgent: UserAgent): ComputerActions
  }
}

