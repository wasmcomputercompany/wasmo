package com.wasmo.home

import com.wasmo.accounts.Client
import okhttp3.HttpUrl

class HomePage(
  val baseUrl: HttpUrl,
  val client: Client,
) {
  fun get(): AppPage {
    return AppPage(
      baseUrl = baseUrl,
    )
  }
}
