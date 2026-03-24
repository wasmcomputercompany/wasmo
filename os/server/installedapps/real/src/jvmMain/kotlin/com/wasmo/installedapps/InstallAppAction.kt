package com.wasmo.installedapps

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.computers.AppManifestAddress.Companion.toAppManifestAddress
import com.wasmo.computers.ComputerStore
import com.wasmo.db.WasmoDb
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(CallScope::class)
class InstallAppAction(
  private val client: Client,
  private val computerStore: ComputerStore,
  private val wasmoDb: WasmoDb,
) {
  suspend fun install(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    val computer = wasmoDb.transactionWithResult(noEnclosing = true) {
      computerStore.getOrNull(client, computerSlug)
        ?: throw NotFoundUserException("unexpected computer: ${computerSlug.value}")
    }

    val appManifestAddress = request.appManifestAddress.toAppManifestAddress()

    val manifest = computer.manifestLoader.load(
      appManifestAddress = appManifestAddress,
    )

    wasmoDb.transactionWithResult(noEnclosing = true) {
      computer.enqueueInstall(
        appManifestAddress = appManifestAddress,
        appManifest = manifest,
      )
    }

    return Response(
      body = InstallAppResponse(
        url = "TODO",
      ),
    )
  }
}
