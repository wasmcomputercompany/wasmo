package com.wasmo.admin.server

import com.wasmo.admin.api.InstallAppRequest
import com.wasmo.admin.api.InstallAppResponse
import com.wasmo.admin.db.AdminDbService
import kotlin.time.Clock

class InstallAppAction(
  private val clock: Clock,
  private val appLoader: AppLoader,
  private val adminDbService: AdminDbService,
) {
  suspend fun installApp(
    request: InstallAppRequest,
  ): InstallAppResponse {

    val manifest = appLoader.loadManifest(request.manifestUrl)
    val wasm = appLoader.loadWasm(manifest)

    adminDbService.appInstallsQueries.insertAppInstall(
      created_at = clock.now(),
      version = manifest.version,
      display_name = manifest.displayName,
      canonical_url = manifest.canonicalUrl,
    )

    return InstallAppResponse(url = "")
  }
}
