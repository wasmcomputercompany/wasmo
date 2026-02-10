package com.wasmo.apps

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.computers.ComputerStore
import com.wasmo.framework.Response

class InstallAppAction(
  private val computerStore: ComputerStore,
) {
  suspend fun install(
    computerSlug: String,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    val computer = computerStore.get(computerSlug)
    computer.installApp(
      manifest = computer.appLoader.loadManifest(
        manifestUrl = request.manifestUrl,
      ),
    )

    return Response(
      body = InstallAppResponse(
        url = "TODO",
      ),
    )
  }
}
