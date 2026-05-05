package com.wasmo.installedapps

import com.wasmo.framework.UserAgent

interface InstalledAppActions {
  val callAppAction: CallAppAction

  interface Factory {
    fun create(userAgent: UserAgent): InstalledAppActions
  }
}
