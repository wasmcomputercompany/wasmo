package com.wasmo.accounts

import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.framework.Response

class ConfirmEmailAddressAction(
  private val client: Client,
) {
  fun confirm(
    request: ConfirmEmailAddressRequest,
  ): Response<ConfirmEmailAddressResponse> {
    return Response(
      body = ConfirmEmailAddressResponse(
        success = true,
        hasMoreAttempts = true,
      ),
    )
  }
}
