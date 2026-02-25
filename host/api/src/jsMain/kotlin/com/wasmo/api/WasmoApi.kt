package com.wasmo.api

interface WasmoApi {
  suspend fun registerPasskey(
    request: RegisterPasskeyRequest,
  ): RegisterPasskeyResponse

  suspend fun authenticatePasskey(
    request: AuthenticatePasskeyRequest,
  ): AuthenticatePasskeyResponse

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

  suspend fun createComputer(
    request: CreateComputerRequest,
  ): CreateComputerResponse
}
