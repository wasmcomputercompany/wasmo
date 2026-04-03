package com.wasmo.testing

import com.wasmo.framework.NotFoundUserException
import com.wasmo.identifiers.OsScope
import com.wasmo.payments.Address
import com.wasmo.payments.CheckoutSession
import com.wasmo.payments.CheckoutStatus
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.CreateCheckoutSessionResponse
import com.wasmo.payments.Customer
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.Subscription
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Inject
@SingleIn(OsScope::class)
class FakePaymentsService(
  private val clock: Clock,
) : PaymentsService {
  private val entries = mutableListOf<Entry>()
  private var nextCheckoutSessionId = 400_400_400
  private var nextClientSecret = 500_500_500
  private var nextSubscriptionId = 600_600_600
  private var nextCustomerId = 700_700_700

  override fun createCheckoutSession(
    request: CreateCheckoutSessionRequest,
  ): CreateCheckoutSessionResponse {
    val now = clock.now()
    val entry = Entry(
      checkoutSessionId = "checkout_session_${nextCheckoutSessionId++}",
      computerSpecToken = request.computerSpecToken,
      clientSecret = "client_secret_${nextClientSecret++}",
      subscriptionId = "subscription_${nextSubscriptionId++}",
      customerId = "customer_${nextCustomerId++}",
      currentPeriodStart = now,
      currentPeriodEnd = now.plus(24.hours),
    )

    entries += entry

    return CreateCheckoutSessionResponse(
      clientSecret = entry.clientSecret,
    )
  }

  override fun getCheckoutSession(checkoutSessionId: String): CheckoutSession {
    val entry = entries.firstOrNull { it.checkoutSessionId == checkoutSessionId }
      ?: throw NotFoundUserException()
    return entry.checkoutSession
  }

  override fun getSubscription(subscriptionId: String): Subscription {
    val entry = entries.firstOrNull { it.subscription.id == subscriptionId }
      ?: throw NotFoundUserException()
    return entry.subscription
  }

  /** Returns the checkout session ID of the completed payment. */
  fun completePayment(clientSecret: String): String {
    val entry = entries.firstOrNull { it.clientSecret == clientSecret }
      ?: throw NotFoundUserException()
    entry.completePayment()
    return entry.checkoutSessionId
  }

  private class Entry(
    val checkoutSessionId: String,
    computerSpecToken: String,
    subscriptionId: String,
    customerId: String,
    val clientSecret: String,
    val currentPeriodStart: Instant,
    val currentPeriodEnd: Instant,
  ) {
    var checkoutSession = CheckoutSession(
      status = CheckoutStatus.Open,
      subscriptionId = subscriptionId,
    )

    var subscription = Subscription(
      id = subscriptionId,
      computerSpecToken = computerSpecToken,
      currentPeriodStart = currentPeriodStart,
      currentPeriodEnd = currentPeriodEnd,
      customer = Customer(
        id = customerId,
        name = "Jennifer Wilson",
        email = "jjwilson@example.com",
        address = Address(
          country = "CA",
          postalCode = "H0H0H0",
        ),
      ),
    )

    fun completePayment() {
      checkoutSession = checkoutSession.copy(
        status = CheckoutStatus.Complete,
      )
    }
  }
}
