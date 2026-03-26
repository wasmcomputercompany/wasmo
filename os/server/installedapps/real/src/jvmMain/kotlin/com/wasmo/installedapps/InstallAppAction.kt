package com.wasmo.installedapps

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.computers.ComputerStore
import com.wasmo.db.WasmoDb
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
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

    val wasmoFileAddress = request.appManifestAddress.toWasmoFileAddress()

    wasmoDb.transactionWithResult(noEnclosing = true) {
      computer.enqueueInstall(
        wasmoFileAddress = wasmoFileAddress,
        slug = request.appSlug,
      )
    }

    return Response(
      body = InstallAppResponse(
        url = "TODO",
      ),
    )
  }
}
