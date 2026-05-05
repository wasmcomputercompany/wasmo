package com.wasmo.website

import com.wasmo.accounts.CallScope
import com.wasmo.framework.HttpAction
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.Url
import com.wasmo.framework.UserAgent
import com.wasmo.framework.asResponse
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.redirect
import com.wasmo.framework.toHttpUrl
import com.wasmo.identifiers.Deployment
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject

@Inject
@ClassKey(FallbackHttpAction::class)
@ContributesIntoMap(CallScope::class)
class FallbackHttpAction(
  deployment: Deployment,
) : HttpAction {
  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  override suspend fun invoke(
    userAgent: UserAgent,
    url: Url,
    request: Request,
  ): Response<ResponseBody> {
    return when {
      url.topPrivateDomain != rootUrl.topPrivateDomain || url.subdomain != rootUrl.subdomain ->
        redirect(rootUrl.toHttpUrl())

      else -> NotFoundUserException().asResponse()
    }
  }
}
