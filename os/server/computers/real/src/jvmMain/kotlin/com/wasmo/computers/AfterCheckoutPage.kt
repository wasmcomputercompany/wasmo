package com.wasmo.computers

import com.wasmo.accounts.CallScope
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.toHttpUrl
import com.wasmo.calls.CallDataService
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.redirect
import com.wasmo.payments.CheckoutStatus
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

/**
 * We navigate to `/after-checkout/{CHECKOUT_SESSION_ID}` after the Stripe checkout screen.
 *
 * This will either redirect to the [BuildYoursRoute] to resume editing (if the payment failed),
 * or to [ComputerHomeRoute] if payment succeeded.
 */
@Inject
@SingleIn(CallScope::class)
class AfterCheckoutPage(
  private val callDataService: CallDataService,
  private val paymentsService: PaymentsService,
  private val wasmoDb: SqlDatabase,
  private val subscriptionUpdater: SubscriptionUpdater,
) {
  suspend fun get(checkoutSessionId: String): Response<ResponseBody> {
    val session = paymentsService.getCheckoutSession(checkoutSessionId)

    val routeCodec = wasmoDb.transaction {
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
