package com.wasmo.accounts

import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.framework.Response
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ClientScope::class)
class ConfirmEmailAddressAction() {
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
