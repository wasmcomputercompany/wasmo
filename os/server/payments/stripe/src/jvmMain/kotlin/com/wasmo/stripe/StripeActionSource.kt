package com.wasmo.stripe

import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class StripeActionSource(
  private val stripeActionsFactory: StripeActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: Binder)
  override fun bindActions() {
    binder.httpAction(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/after-checkout/{checkoutSessionId}",
      ),
    ) { userAgent, url, _ ->
      val action = stripeActionsFactory.create(userAgent).afterCheckoutPage
      action.get(url.path[1])
    }
  }
}
