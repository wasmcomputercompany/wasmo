package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.db.WasmoDb
import com.wasmo.framework.ArgumentUserException
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

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

    val manifestUrl = request.manifestUrl.toHttpUrlOrNull()
      ?: throw ArgumentUserException("unexpected manifest URL: ${request.manifestUrl}")

    computer.enqueueInstallApp(
      manifestUrl = manifestUrl,
    )

    return Response(
      body = InstallAppResponse(
        url = "TODO",
      ),
    )
  }
}
