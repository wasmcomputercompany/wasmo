package com.wasmo.home

import com.wasmo.accounts.Client
import com.wasmo.deployment.Deployment

class HomePage(
  val deployment: Deployment,
  val client: Client,
) {
  fun get(): AppPage {
    return AppPage(
      baseUrl = deployment.baseUrl,
    )
  }
}
