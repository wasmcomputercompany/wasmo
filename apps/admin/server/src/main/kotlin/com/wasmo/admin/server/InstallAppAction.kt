package com.wasmo.admin.server

import com.wasmo.admin.api.InstallAppRequest
import com.wasmo.admin.api.InstallAppResponse

class InstallAppAction(
  private val appLoader: AppLoader,
) {
  suspend fun installApp(
    request: InstallAppRequest,
  ): InstallAppResponse {

    val manifest = appLoader.loadManifest(request.manifestUrl)
    val wasm = appLoader.loadWasm(manifest)

    // TODO: write the manifest to the DB

    return InstallAppResponse(url = "")
  }
}
