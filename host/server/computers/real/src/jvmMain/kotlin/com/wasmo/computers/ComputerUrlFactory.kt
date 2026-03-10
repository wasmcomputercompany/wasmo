package com.wasmo.computers

import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl

@Inject
@SingleIn(ComputerScope::class)
class ComputerUrlFactory(
  private val deployment: Deployment,
  private val slug: ComputerSlug,
) {
  val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug.${deployment.baseUrl.host}")
      .build()

  fun appUrl(appSlug: AppSlug): HttpUrl = deployment.baseUrl.newBuilder()
    .host("$appSlug-$slug.${deployment.baseUrl.host}")
    .build()
}
