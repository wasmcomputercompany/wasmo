package com.wasmo.api

import com.wasmo.api.payments.ComputerPaymentMethod
import com.wasmo.api.routes.RoutingContext

interface OsHtml {
  val computerPaymentMethod: ComputerPaymentMethod
  val accountSnapshot: AccountSnapshot
  val routingContext: RoutingContext
  val inviteTicket: InviteTicket?
  val computerSnapshot: ComputerSnapshot?
  val computerListSnapshot: ComputerListSnapshot?
  val signInSnapshot: SignInSnapshot?
}
