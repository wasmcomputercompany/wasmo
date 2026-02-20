package com.wasmo.stripe

import com.stripe.model.checkout.Session
import com.wasmo.accounts.Client
import com.wasmo.api.stripe.GetSessionStatusRequest
import com.wasmo.api.stripe.GetSessionStatusResponse
import com.wasmo.framework.Response

class GetSessionStatusAction(
  val stripeInitializer: StripeInitializer,
  val client: Client,
) {
  fun get(request: GetSessionStatusRequest): Response<GetSessionStatusResponse> {
    stripeInitializer.requireInitialized()
    val session = Session.retrieve(request.sessionId)
    return Response(
      body = GetSessionStatusResponse(
        status = session.status,
        customer_email = session.customerDetails.email,
      ),
    )
  }
}
