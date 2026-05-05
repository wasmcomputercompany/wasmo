package com.wasmo.emails

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Client
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.emails.messages.challengeCodeEmailMessage
import com.wasmo.framework.Response
import com.wasmo.framework.RpcAction
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.identifiers.Deployment
import com.wasmo.sendemail.SendEmailService
import com.wasmo.support.tokens.newChallengeCode
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@ClassKey(LinkEmailAddressRpc::class)
@ContributesIntoMap(CallScope::class, binding = binding<RpcAction<*, *>>())
class LinkEmailAddressRpc(
  private val client: Client,
  private val deployment: Deployment,
  private val sendEmailService: SendEmailService,
  private val challengeTokenChecker: ChallengeTokenChecker,
  private val wasmDb: SqlDatabase,
) : RpcAction<LinkEmailAddressRequest, LinkEmailAddressResponse> {
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

  override suspend fun invoke(
    userAgent: UserAgent,
    request: LinkEmailAddressRequest,
    url: Url,
  ) = link(request)
}
