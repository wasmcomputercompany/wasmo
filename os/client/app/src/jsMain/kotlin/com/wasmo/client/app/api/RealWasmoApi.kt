package com.wasmo.client.app.api

import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.api.WasmoApi
import com.wasmo.api.WasmoJson
import com.wasmo.client.identifiers.ClientAppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer

@Inject
@SingleIn(ClientAppScope::class)
class RealWasmoApi : WasmoApi {
  override suspend fun registerPasskey(
    request: RegisterPasskeyRequest,
  ): RegisterPasskeyResponse {
    return call("/register-passkey", request)
  }

  override suspend fun authenticatePasskey(
    request: AuthenticatePasskeyRequest,
  ): AuthenticatePasskeyResponse {
    return call("/authenticate-passkey", request)
  }

  override suspend fun installApp(
    request: InstallAppRequest,
  ): InstallAppResponse {
    return call("/install-app", request)
  }

  override suspend fun linkEmailAddress(
    request: LinkEmailAddressRequest,
  ): LinkEmailAddressResponse {
    return call("/link-email-address", request)
  }

  override suspend fun confirmEmailAddress(
    request: ConfirmEmailAddressRequest,
  ): ConfirmEmailAddressResponse {
    return call("/confirm-email-address", request)
  }

  override suspend fun createComputerSpec(
    request: CreateComputerSpecRequest,
  ): CreateComputerSpecResponse {
    return call("/create-computer-spec", request)
  }

  suspend inline fun <reified S, reified R> call(
    path: String,
    request: S,
  ): R {
    val requestSerializer = serializer<S>()
    val responseDeserializer = serializer<R>()

    val requestJson = WasmoJson.encodeToString(requestSerializer, request)
    val request = js(
      """
      {
        method: "POST",
        body: requestJson,
      }
      """,
    )
    val response = window.fetch(path, request).await()

    if (response.status !in 200..<300) {
      throw Exception("unexpected response code: ${response.status}")
    }

    val json = response.json().await()
    return WasmoJson.decodeFromDynamic(responseDeserializer, json)
  }
}
