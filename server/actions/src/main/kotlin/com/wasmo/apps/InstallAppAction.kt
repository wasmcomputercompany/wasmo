package com.wasmo.apps

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.computers.ComputerStore
import com.wasmo.framework.Response

class InstallAppAction(
  private val computerStore: ComputerStore,
  private val appLoader: AppLoader,
) {
  suspend fun install(
    computerSlug: String,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    val computer = computerStore.get(computerSlug)

    val manifest = appLoader.loadManifest(request.manifestUrl)

    computer.installApp(manifest)

    return Response(
      body = InstallAppResponse(
        url = "TODO",
      ),
    )
  }
}
