package com.wasmo.client.app.data

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.WasmoApi
import com.wasmo.client.identifiers.ClientAppScope
import com.wasmo.identifiers.AppSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ClientAppScope::class)
class ComputerDataService(
  private val wasmoApi: WasmoApi,
) {
  suspend fun install(appManifestAddress: String, slug: AppSlug) {
    wasmoApi.installApp(InstallAppRequest(appManifestAddress, slug))
  }
}
