package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.accounts.ClientScope
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
import com.wasmo.db.WasmoDb
import com.wasmo.framework.Response
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ClientScope::class)
class CreateComputerAction(
  private val paymentsService: PaymentsService,
  private val client: Client,
  private val wasmoDb: WasmoDb,
  private val computerSpecStore: ComputerSpecStore,
) {
  fun create(
    request: CreateComputerRequest,
  ): Response<CreateComputerResponse> {
    wasmoDb.transactionWithResult(noEnclosing = true) {
      computerSpecStore.createSpec(
        accountId = client.getOrCreateAccountId(),
        slug = request.slug,
        computerSpecToken = request.computerSpecToken,
      )
    }

    val checkoutSession = paymentsService.createCheckoutSession(
      CreateCheckoutSessionRequest(request.computerSpecToken),
    )

    return Response(
      body = CreateComputerResponse(
        checkoutSessionClientSecret = checkoutSession.clientSecret,
      ),
    )
  }
}
