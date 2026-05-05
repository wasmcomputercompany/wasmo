package com.wasmo.installedapps

import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class InstalledAppActionSource(
  private val installedAppActionsFactory: InstalledAppActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.appRegex) {
      routeAll {
        httpAction { userAgent, _, request ->
          val action = installedAppActionsFactory.create(userAgent).callAppAction
          action.call(request)
        }
      }
    }
  }
}
