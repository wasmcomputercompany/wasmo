package com.wasmo.stripe

import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object StripeActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    stripeActionsFactory: StripeActions.Factory,
    hostnamePatterns: HostnamePatterns,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Http(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/after-checkout/{checkoutSessionId}",
      ),
    ) { userAgent, url, _ ->
      val action = stripeActionsFactory.create(userAgent).afterCheckoutPage
      action.get(url.path[1])
    },
  )
}
