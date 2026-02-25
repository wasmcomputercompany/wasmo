package com.wasmo.stripe

import com.stripe.service.checkout.SessionService
import com.wasmo.accounts.Client
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.deployment.Deployment
import com.wasmo.framework.NotFoundException
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.redirect

class AfterCheckoutAction(
  val sessionService: SessionService,
  val subscriptionUpdater: SubscriptionUpdater,
  val routeCodec: RouteCodec,
  val deployment: Deployment,
  val client: Client,
) {
  fun get(checkoutSessionId: String): Response<ResponseBody> {
    val session = sessionService.retrieve(checkoutSessionId)
    when (session.status) {
      "open" -> {
        val url = routeCodec.encode(BuildYoursRoute)
        return redirect(url.toHttpUrl())
      }

      "complete" -> {
        val snapshot = subscriptionUpdater.update(session.subscription)
        val url = routeCodec.encode(ComputerHomeRoute(snapshot.slug))
        return redirect(url.toHttpUrl())
      }

      else -> throw NotFoundException()
    }
  }
}
