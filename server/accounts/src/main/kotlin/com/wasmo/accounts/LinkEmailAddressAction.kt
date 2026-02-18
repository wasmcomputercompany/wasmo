package com.wasmo.accounts

import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.Response

class LinkEmailAddressAction(
  private val client: Client,
) {
  fun link(
    request: LinkEmailAddressRequest,
  ): Response<LinkEmailAddressResponse> {
    return Response(
      body = LinkEmailAddressResponse(
        challengeSent = true,
      ),
    )
  }
}
