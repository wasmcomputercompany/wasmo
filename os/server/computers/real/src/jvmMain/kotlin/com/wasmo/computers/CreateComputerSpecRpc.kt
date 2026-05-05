package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(CreateComputerSpecRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class CreateComputerSpecRpc(
  private val paymentsService: PaymentsService,
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val computerSpecStore: ComputerSpecStore,
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
    }

    val checkoutSession = paymentsService.createCheckoutSession(
      CreateCheckoutSessionRequest(request.computerSpecToken),
    )

    return Response(
      body = CreateComputerSpecResponse(
        checkoutSessionClientSecret = checkoutSession.clientSecret,
      ),
    )
  }

  override suspend fun invoke(
    userAgent: UserAgent,
    request: CreateComputerSpecRequest,
    url: Url,
  ) = create(request)
}
