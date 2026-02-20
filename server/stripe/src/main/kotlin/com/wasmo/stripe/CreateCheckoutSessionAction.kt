package com.wasmo.stripe

import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.wasmo.accounts.Client
import com.wasmo.api.stripe.CreateCheckoutSessionRequest
import com.wasmo.api.stripe.CreateCheckoutSessionResponse
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response

class CreateCheckoutSessionAction(
  val stripeInitializer: StripeInitializer,
  val catalog: Catalog,
  val deployment: Deployment,
  val client: Client,
) {
  fun create(
    request: CreateCheckoutSessionRequest,
  ): Response<CreateCheckoutSessionResponse> {
    stripeInitializer.requireInitialized()

    val sessionCreateParams = SessionCreateParams.builder()
      .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
      .setMode(SessionCreateParams.Mode.PAYMENT)
      .setReturnUrl(
        deployment.baseUrl.resolve("/return.html?session_id={CHECKOUT_SESSION_ID}").toString(),
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

    val checkoutSession = Session.create(sessionCreateParams)

    return Response(
      body = CreateCheckoutSessionResponse(
        clientSecret = checkoutSession.clientSecret,
      ),
    )
  }
}
