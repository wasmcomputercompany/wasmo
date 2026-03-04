package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.ComputerSlug
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.db.WasmoDb
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(CallScope::class)
class InstallAppAction(
  private val client: Client,
  private val computerStore: ComputerStore,
  private val manifestLoader: ManifestLoader,
  private val wasmoDb: WasmoDb,
) {
  suspend fun install(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    val appManifest = manifestLoader.loadManifest(
      manifestUrl = request.manifestUrl,
    )
    wasmoDb.transactionWithResult(noEnclosing = true) {
      val computer = computerStore.get(client, computerSlug)
      computer.installApp(
        manifestUrl = request.manifestUrl,
        manifest = appManifest,
      )
    }

    return Response(
      body = InstallAppResponse(
        url = "TODO",
      ),
    )
  }
}
