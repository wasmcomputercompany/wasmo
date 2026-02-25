package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.PaymentsService

class CreateComputerAction(
  val paymentsService: PaymentsService,
  val client: Client,
  val wasmoDbService: WasmoDbService,
  val computerSpecStore: ComputerSpecStore,
) {
  fun create(
    request: CreateComputerRequest,
  ): Response<CreateComputerResponse> {
    wasmoDbService.transactionWithResult(noEnclosing = true) {
      computerSpecStore.createSpec(
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
