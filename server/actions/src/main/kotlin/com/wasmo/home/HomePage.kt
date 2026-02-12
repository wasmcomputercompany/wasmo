package com.wasmo.home

import okhttp3.HttpUrl

class HomePage(
  val baseUrl: HttpUrl,
) {
  fun get(): AppPage {
    return AppPage(
      baseUrl = baseUrl,
    )
  }
}
