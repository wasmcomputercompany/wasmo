package com.wasmo.admin.server

import com.wasmo.admin.api.InstallAppRequest
import com.wasmo.admin.api.InstallAppResponse

class InstallAppAction {
  fun installApp(
    request: InstallAppRequest,
  ): InstallAppResponse {
    return InstallAppResponse(url = "")
  }
}
