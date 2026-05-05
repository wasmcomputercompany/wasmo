package com.wasmo.website

import com.wasmo.framework.ActionSource
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class WebsiteActionSource(
  private val websiteActionsFactory: WebsiteActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: ActionSource.Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.computerRegex) {
      route("/") {
        httpAction { userAgent, url, _ ->
          val action = websiteActionsFactory.create(userAgent).osPage
          action.get(url).response
        }
      }

      // TODO: change the web app to always fetch static resources from the root URL.
      staticResources("/", "static")
    }
  }
}
