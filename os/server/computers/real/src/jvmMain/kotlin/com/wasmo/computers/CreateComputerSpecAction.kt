package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.app.db.WasmoDb
import com.wasmo.framework.Response
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(CallScope::class)
class CreateComputerSpecAction(
  private val paymentsService: PaymentsService,
  private val client: Client,
  private val wasmoDb: WasmoDb,
  private val computerSpecStore: ComputerSpecStore,
) {
  fun create(
    request: CreateComputerSpecRequest,
  ): Response<CreateComputerSpecResponse> {
    wasmoDb.transactionWithResult(noEnclosing = true) {
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
