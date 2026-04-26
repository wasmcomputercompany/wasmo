package com.wasmo.emails

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.deployment.Deployment
import com.wasmo.emails.messages.challengeCodeEmailMessage
import com.wasmo.framework.Response
import com.wasmo.sendemail.SendEmailService
import com.wasmo.sql.transaction
import com.wasmo.support.tokens.newChallengeCode
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase

@Inject
@SingleIn(CallScope::class)
class LinkEmailAddressRpc(
  private val client: Client,
  private val deployment: Deployment,
  private val sendEmailService: SendEmailService,
  private val challengeTokenChecker: ChallengeTokenChecker,
  private val wasmDb: SqlDatabase,
) {
  suspend fun link(
    request: LinkEmailAddressRequest,
  ): Response<LinkEmailAddressResponse> {
    val challengeCode = newChallengeCode()

    val accountId = wasmDb.transaction {
      client.getOrCreateAccountId()
    }

    val challengeToken = challengeTokenChecker.create(
      accountId = accountId,
      emailAddress = request.unverifiedEmailAddress,
      challengeCode = challengeCode,
    )

    sendEmailService.send(
      message = challengeCodeEmailMessage(
        from = deployment.sendFromEmailAddress,
        to = request.unverifiedEmailAddress,
        baseUrl = deployment.baseUrl.toString(),
        baseUrlHost = deployment.baseUrl.host,
        challengeCode = challengeCode,
      ),
    )

    return Response(
      body = LinkEmailAddressResponse(
        challengeToken = challengeToken,
      ),
    )
  }
}
