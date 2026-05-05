package com.wasmo.ktor

import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class InstalledAppActionSource(
  private val callGraphFactory: NewCallGraphFactory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 1

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.appRegex) {
      routeAll {
        httpAction { userAgent, _, request ->
          val callGraph = callGraphFactory.create(userAgent)
          callGraph.callAppAction.call(request)
        }
      }
    }
  }
}
