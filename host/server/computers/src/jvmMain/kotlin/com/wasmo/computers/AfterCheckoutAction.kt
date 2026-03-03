package com.wasmo.computers

import com.wasmo.accounts.Client
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.app.db.WasmoDbService
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.redirect
import com.wasmo.payments.CheckoutStatus
import com.wasmo.payments.PaymentsService

/**
 * We navigate to `/after-checkout/{CHECKOUT_SESSION_ID}` after the Stripe checkout screen.
 *
 * This will either redirect to the [BuildYoursRoute] to resume editing (if the payment failed),
 * or to [ComputerHomeRoute] if payment succeeded.
 */
class AfterCheckoutAction(
  val callDataServiceFactory: CallDataService.Factory,
  val paymentsService: PaymentsService,
  val wasmoDbService: WasmoDbService,
  val subscriptionUpdater: SubscriptionUpdater,
  val client: Client,
) {
  fun get(checkoutSessionId: String): Response<ResponseBody> {
    val session = paymentsService.getCheckoutSession(checkoutSessionId)

    val callDataService = callDataServiceFactory.create(client)
    val routeCodec = wasmoDbService.transactionWithResult(noEnclosing = true) {
      callDataService.routeCodec()
    }

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
    }
  }
}
