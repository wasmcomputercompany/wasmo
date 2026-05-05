package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.identifiers.AppSlugRegex
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.WasmoFileAddress.Companion.toWasmoFileAddress
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(InstallAppRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class InstallAppRpc(
  private val client: Client,
  private val computerStore: ComputerStore,
  private val wasmoDb: SqlDatabase,
) : RpcAction<InstallAppRequest, InstallAppResponse> {
  suspend fun install(
    computerSlug: ComputerSlug,
    request: InstallAppRequest,
  ): Response<InstallAppResponse> {
    check(request.appSlug.value.matches(AppSlugRegex)) {
      """
      |unexpected app slug '${request.appSlug}'
      |must be 1-15 characters and match ${AppSlugRegex.pattern}
      """.trimMargin()
    }

    val computer = wasmoDb.transaction {
      computerStore.getOrNull(client, computerSlug)
        ?: throw NotFoundUserException("unexpected computer: ${computerSlug.value}")
    }

    val wasmoFileAddress = request.appManifestAddress.toWasmoFileAddress()

    wasmoDb.transaction {
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

  override suspend fun invoke(
    userAgent: UserAgent,
    request: InstallAppRequest,
    url: Url,
  ) = install(
    ComputerSlug(value = url.subdomain ?: throw NotFoundUserException()),
    request,
  )
}
