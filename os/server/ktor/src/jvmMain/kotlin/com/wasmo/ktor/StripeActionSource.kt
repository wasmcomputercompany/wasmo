package com.wasmo.ktor

import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class StripeActionSource(
  private val callGraphFactory: NewCallGraphFactory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 4

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.osHostname) {
      route("/after-checkout/{checkoutSessionId}") {
        httpAction { userAgent, url, _ ->
          val callGraph = callGraphFactory.create(userAgent)
          callGraph.afterCheckoutPage.get(url.path[1])
        }
      }
    }
  }
}
