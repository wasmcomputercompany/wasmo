package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.framework.Response
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@SingleIn(CallScope::class)
class CreateComputerSpecRpc(
  private val paymentsService: PaymentsService,
  private val client: Client,
  private val wasmoDb: SqlDatabase,
  private val computerSpecStore: ComputerSpecStore,
) {
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
}
