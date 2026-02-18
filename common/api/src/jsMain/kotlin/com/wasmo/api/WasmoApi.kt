package com.wasmo.api

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
}
