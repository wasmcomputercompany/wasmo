package com.wasmo.stripe

import com.stripe.service.checkout.SessionService
import com.wasmo.accounts.Client
import com.wasmo.api.stripe.GetSessionStatusRequest
import com.wasmo.api.stripe.GetSessionStatusResponse
import com.wasmo.framework.Response

class GetSessionStatusAction(
  val sessionService: SessionService,
  val client: Client,
) {
  fun get(request: GetSessionStatusRequest): Response<GetSessionStatusResponse> {
    val session = sessionService.retrieve(request.sessionId)
    return Response(
      body = GetSessionStatusResponse(
        status = session.status,
        customer_email = session.customerDetails.email,
      ),
    )
  }
}
