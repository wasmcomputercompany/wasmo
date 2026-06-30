package com.wasmo.computers.free

import com.wasmo.payments.CheckoutSession
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.CreateCheckoutSessionResponse
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.Subscription

class NoPaymentsService : PaymentsService {
  override fun createCheckoutSession(request: CreateCheckoutSessionRequest): CreateCheckoutSessionResponse {
    throw UnsupportedOperationException("No payments service available")
  }

  override fun getCheckoutSession(checkoutSessionId: String): CheckoutSession {
    throw UnsupportedOperationException("No payments service available")
  }

  override fun getSubscription(subscriptionId: String): Subscription {
    // TBD: Should we create a DbSubscriptionPeriod even for computers that are free?
    throw UnsupportedOperationException("No payments service available")
  }
}
