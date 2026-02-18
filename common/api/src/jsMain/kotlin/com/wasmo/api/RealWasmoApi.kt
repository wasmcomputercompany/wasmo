package com.wasmo.api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.serializer

class RealWasmoApi : WasmoApi {
  override suspend fun createComputer(
    request: CreateComputerRequest,
  ): CreateComputerResponse {
    return call("/create-computer", request)
  }

  override suspend fun installApp(
    computer: String,
    request: InstallAppRequest,
  ): InstallAppResponse {
    return call("/computers/${computer}/install-app", request)
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
