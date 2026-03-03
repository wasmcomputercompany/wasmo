package com.wasmo.stripe

import com.stripe.param.SubscriptionRetrieveParams
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.service.SubscriptionService
import com.stripe.service.checkout.SessionService
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.payments.Address
import com.wasmo.payments.CheckoutSession
import com.wasmo.payments.CheckoutStatus
import com.wasmo.payments.CreateCheckoutSessionRequest
import com.wasmo.payments.CreateCheckoutSessionResponse
import com.wasmo.payments.Customer
import com.wasmo.payments.PaymentsService
import com.wasmo.payments.Subscription
import kotlin.time.Instant

class StripePaymentsService(
  private val deployment: Deployment,
  private val sessionService: SessionService,
  private val subscriptionService: SubscriptionService,
  private val catalog: Catalog,
) : PaymentsService {
  override fun createCheckoutSession(
    request: CreateCheckoutSessionRequest,
  ): CreateCheckoutSessionResponse {
    val sessionCreateParams = SessionCreateParams.builder()
      .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
      .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
      .setReturnUrl("${deployment.baseUrl.resolve("/after-checkout/")}{CHECKOUT_SESSION_ID}")
      .setSubscriptionData(
        SessionCreateParams.SubscriptionData.Builder()
          .putMetadata(StripeMetadataKey.ComputerSpecToken.name, request.computerSpecToken)
          .build(),
      )
      .setAutomaticTax(
        SessionCreateParams.AutomaticTax.builder()
          .setEnabled(true)
          .build(),
      )
      .addLineItem(
        SessionCreateParams.LineItem.builder()
          .setQuantity(1L)
          .setPrice(catalog.wasmoStandard.priceId)
          .build(),
      )
      .build()
    val checkoutSession = sessionService.create(sessionCreateParams)

    return CreateCheckoutSessionResponse(
      clientSecret = checkoutSession.clientSecret,
    )
  }

  override fun getCheckoutSession(checkoutSessionId: String): CheckoutSession {
    val session = sessionService.retrieve(checkoutSessionId)

    return CheckoutSession(
      status = when (session.status) {
        "open" -> CheckoutStatus.Open
        "complete" -> CheckoutStatus.Complete
        else -> error("unexpected session status: ${session.status}")
      },
      subscriptionId = session.subscription,
    )
  }

  override fun getSubscription(subscriptionId: String): Subscription {
    val subscription = subscriptionService.retrieve(
      subscriptionId,
      SubscriptionRetrieveParams.Builder()
        .addExpand("customer")
        .build(),
    )

    val item = subscription.items.data.single()
    require(item.price.id == catalog.wasmoStandard.priceId)
    require(item.quantity == 1L)

    return Subscription(
      id = subscription.id,
      computerSpecToken = subscription.metadata[StripeMetadataKey.ComputerSpecToken.name]
        ?: error("expected metadata not found: ${StripeMetadataKey.ComputerSpecToken}"),
      currentPeriodStart = Instant.fromEpochSeconds(item.currentPeriodStart),
      currentPeriodEnd = Instant.fromEpochSeconds(item.currentPeriodEnd),
      customer = subscription.customerObject.toWasmo(),
    )
  }

  private fun com.stripe.model.Customer.toWasmo() = Customer(
    id = this.id,
    name = this.name,
    email = this.email,
    address = this.address.toWasmo(),
  )

  private fun com.stripe.model.Address.toWasmo() = Address(
    country = country,
    postalCode = postalCode,
  )
}
