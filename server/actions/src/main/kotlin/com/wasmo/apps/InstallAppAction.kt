package com.wasmo.apps

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.BadRequestException
import com.wasmo.framework.Response
import kotlin.time.Clock

class InstallAppAction(
  private val clock: Clock,
  private val appLoader: AppLoader,
  private val service: WasmoDbService,
) {
  suspend fun install(
    computerSlug: String,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    val manifest = appLoader.loadManifest(request.manifestUrl)
    val wasm = appLoader.loadWasm(manifest)

    val computer = service.computerQueries.selectComputerBySlug(
      slug = computerSlug
    ).executeAsOneOrNull()
      ?: throw BadRequestException("unexpected computer: $computerSlug")

    service.appInstallQueries.insertAppInstall(
      created_at = clock.now(),
      computer_id = computer.id,
      slug = manifest.slug,
      display_name = manifest.displayName,
      version = manifest.version,
    ).executeAsOne()

    return service.transactionWithResult(noEnclosing = true) {
      Response(
        body = InstallAppResponse(
          url = "TODO",
        ),
      )
    }
  }
}
