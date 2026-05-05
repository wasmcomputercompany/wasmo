package com.wasmo.stripe

import com.wasmo.accounts.CallScope
import com.wasmo.api.routes.BuildYoursRoute
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.calls.CallDataService
import com.wasmo.framework.HttpAction
import com.wasmo.framework.Request
import com.wasmo.framework.Response
import com.wasmo.framework.ResponseBody
import com.wasmo.framework.Url
import com.wasmo.framework.redirect
import com.wasmo.framework.toHttpUrl
import com.wasmo.payments.CheckoutStatus
import com.wasmo.payments.PaymentsService
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

/**
 * We navigate to `/after-checkout/{CHECKOUT_SESSION_ID}` after the Stripe checkout screen.
 *
 * This will either redirect to the [BuildYoursRoute] to resume editing (if the payment failed),
 * or to [ComputerHomeRoute] if payment succeeded.
 */
@Inject
@ClassKey(AfterCheckoutPage::class)
@ContributesIntoMap(CallScope::class)
class AfterCheckoutPage(
  private val callDataService: CallDataService,
  private val paymentsService: PaymentsService,
  private val wasmoDb: SqlDatabase,
  private val subscriptionUpdater: SubscriptionUpdater,
) : HttpAction {
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

  override suspend operator fun invoke(
    url: Url,
    request: Request,
  ) = get(url.path[1])
}
