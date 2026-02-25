package com.wasmo.api

import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey

interface AppPage {
  val stripePublishableKey: StripePublishableKey
  val accountSnapshot: AccountSnapshot
  val routingContext: RoutingContext
  val inviteTicket: InviteTicket?
}
