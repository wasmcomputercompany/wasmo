package com.wasmo.deployment

import okhttp3.HttpUrl

data class Deployment(
  val baseUrl: HttpUrl,
  val sendFromEmailAddress: String,
)
