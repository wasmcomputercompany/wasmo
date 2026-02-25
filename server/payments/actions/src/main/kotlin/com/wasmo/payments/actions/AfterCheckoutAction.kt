package com.wasmo.payments.actions

import com.wasmo.accounts.Client
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.framework.NotFoundException
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.redirect
import com.wasmo.payments.CheckoutStatus
import com.wasmo.payments.PaymentsService

class AfterCheckoutAction(
  val paymentsService: PaymentsService,
  val subscriptionUpdater: SubscriptionUpdater,
  val routeCodec: RouteCodec,
  val client: Client,
) {
  fun get(checkoutSessionId: String): Response<ResponseBody> {
    val session = paymentsService.getCheckoutSession(checkoutSessionId)
    when (session.status) {
      CheckoutStatus.Open -> {
        val url = routeCodec.encode(BuildYoursRoute)
        return redirect(url.toHttpUrl())
      }

      CheckoutStatus.Complete -> {
        val snapshot = subscriptionUpdater.update(session.subscriptionId)
        val url = routeCodec.encode(ComputerHomeRoute(snapshot.slug))
        return redirect(url.toHttpUrl())
      }

      else -> throw NotFoundException()
    }
  }
}
