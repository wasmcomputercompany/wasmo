package com.wasmo.payments.actions

import com.wasmo.accounts.Client
import com.wasmo.api.stripe.CreateCheckoutSessionRequest
import com.wasmo.api.stripe.CreateCheckoutSessionResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.framework.Response
import com.wasmo.payments.PaymentsService
import kotlin.time.Clock
import org.postgresql.util.PSQLException

class CreateCheckoutSessionAction(
  val clock: Clock,
  val paymentsService: PaymentsService,
  val client: Client,
  val wasmoDbService: WasmoDbService,
) {
  fun create(
    request: CreateCheckoutSessionRequest,
  ): Response<CreateCheckoutSessionResponse> {
    wasmoDbService.transactionWithResult(noEnclosing = true) {
      try {
        wasmoDbService.computerSpecQueries.insertComputerSpec(
          created_at = clock.now(),
          version = 1,
          token = request.computerSpecToken,
          slug = request.slug,
        ).executeAsOneOrNull()
      } catch (e: PSQLException) {
        // TODO: recover from idempotent inserts
        throw e
      }
    }

    val checkoutSession = paymentsService.createCheckoutSession(
      com.wasmo.payments.CreateCheckoutSessionRequest(request.computerSpecToken)
    )

    return Response(
      body = CreateCheckoutSessionResponse(
        clientSecret = checkoutSession.clientSecret,
      ),
    )
  }
}
