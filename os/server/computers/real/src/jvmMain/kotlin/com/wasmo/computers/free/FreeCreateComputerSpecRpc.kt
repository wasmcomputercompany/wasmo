package com.wasmo.computers.free

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.computers.ComputerSpecStore
import com.wasmo.computers.ComputerStore
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class FreeCreateComputerSpecRpc(
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val computerSpecStore: ComputerSpecStore,
  private val computerStore: ComputerStore,
) : RpcAction<CreateComputerSpecRequest, CreateComputerSpecResponse> {
  suspend fun create(
    request: CreateComputerSpecRequest,
  ): Response<CreateComputerSpecResponse> {
    wasmoDb.transaction {
      computerSpecStore.insertIfAbsent(
        accountId = client.getOrCreateAccountId(),
        slug = request.slug,
        computerSpecToken = request.computerSpecToken,
      )
      computerStore.initializeFromSpec(request.computerSpecToken)
    }
    return Response(
      body = CreateComputerSpecResponse.Success,
    )
  }

  override suspend operator fun invoke(
    request: CreateComputerSpecRequest,
    url: Url,
  ) = create(request)
}
