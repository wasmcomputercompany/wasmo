package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.accounts.ClientScope
import com.wasmo.api.ComputerSlug
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ClientScope::class)
class InstallAppAction(
  private val client: Client,
  private val computerStore: ComputerStore,
  private val wasmoDbService: WasmoDbService,
) {
  suspend fun install(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    val computer = wasmoDbService.transactionWithResult(noEnclosing = true) {
      computerStore.get(client, computerSlug)
    }
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
