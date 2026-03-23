package com.wasmo.client.app.data

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.WasmoApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class ComputerDataService(
  private val wasmoApi: WasmoApi,
) {
  suspend fun install(url: String) {
    wasmoApi.installApp(InstallAppRequest(url))
  }
}
