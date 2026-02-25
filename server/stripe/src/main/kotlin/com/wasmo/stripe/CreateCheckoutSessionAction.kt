package com.wasmo.stripe

import com.stripe.param.checkout.SessionCreateParams
import com.stripe.service.checkout.SessionService
import com.wasmo.accounts.Client
import com.wasmo.api.stripe.CreateCheckoutSessionRequest
import com.wasmo.api.stripe.CreateCheckoutSessionResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.framework.Response
import kotlin.time.Clock
import org.postgresql.util.PSQLException

class CreateCheckoutSessionAction(
  val clock: Clock,
  val sessionService: SessionService,
  val catalog: Catalog,
  val deployment: Deployment,
  val client: Client,
  val wasmoDbService: WasmoDbService,
) {
  fun create(
    request: CreateCheckoutSessionRequest,
  ): Response<CreateCheckoutSessionResponse> {
    wasmoDbService.transactionWithResult(noEnclosing = true) {
      try {
        wasmoDbService.computerSpecQueries.insertComputerSpec(
          created_at = clock.now(),
          version = 1,
          token = request.computerSpecToken,
          slug = request.slug,
        ).executeAsOneOrNull()
      } catch (e: PSQLException) {
        // TODO: recover from idempotent inserts
        throw e
      }
    }

    val sessionCreateParams = SessionCreateParams.builder()
      .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
      .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
      .setReturnUrl("${deployment.baseUrl.resolve("/after-checkout/")}{CHECKOUT_SESSION_ID}")
      .setSubscriptionData(SessionCreateParams.SubscriptionData.Builder()
        .putMetadata(StripeMetadataKey.ComputerSpecToken.name, request.computerSpecToken)
        .build())
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

    return Response(
      body = CreateCheckoutSessionResponse(
        clientSecret = checkoutSession.clientSecret,
      ),
    )
  }
}
