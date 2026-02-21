package com.wasmo.api

import com.wasmo.api.stripe.CreateCheckoutSessionRequest
import com.wasmo.api.stripe.CreateCheckoutSessionResponse
import com.wasmo.api.stripe.GetSessionStatusRequest
import com.wasmo.api.stripe.GetSessionStatusResponse

interface WasmoApi {
  suspend fun createComputer(
    request: CreateComputerRequest,
  ): CreateComputerResponse

  suspend fun installApp(
    computer: String,
    request: InstallAppRequest,
  ): InstallAppResponse

  suspend fun linkEmailAddress(
    request: LinkEmailAddressRequest,
  ): LinkEmailAddressResponse

  suspend fun confirmEmailAddress(
    request: ConfirmEmailAddressRequest,
  ): ConfirmEmailAddressResponse

  suspend fun createCheckoutSession(
    request: CreateCheckoutSessionRequest,
  ): CreateCheckoutSessionResponse

  suspend fun getSessionStatus(
    request: GetSessionStatusRequest,
  ): GetSessionStatusResponse
}
