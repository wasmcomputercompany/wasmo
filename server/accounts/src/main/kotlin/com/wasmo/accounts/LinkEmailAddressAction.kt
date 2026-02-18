package com.wasmo.accounts

import com.wasmo.accounts.emails.challengeCodeEmailMessage
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import com.wasmo.sendemail.SendEmailService

class LinkEmailAddressAction(
  private val deployment: Deployment,
  private val sendEmailService: SendEmailService,
  private val client: Client,
) {
  suspend fun link(
    request: LinkEmailAddressRequest,
  ): Response<LinkEmailAddressResponse> {
    val code = "123456"

    sendEmailService.send(
      message = challengeCodeEmailMessage(
        from = deployment.sendFromEmailAddress,
        to = request.unverifiedEmailAddress,
        baseUrl = deployment.baseUrl.toString(),
        baseUrlHost = deployment.baseUrl.host,
        code = code,
      )
    )

    return Response(
      body = LinkEmailAddressResponse(
        challengeSent = true,
      ),
    )
  }
}
