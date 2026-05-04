package com.wasmo.ktor

import com.wasmo.deployment.Deployment
import com.wasmo.framework.HttpActionBinder
import com.wasmo.framework.UserAgent
import com.wasmo.framework.decodeUrl
import com.wasmo.identifiers.AppSlugRegex
import com.wasmo.identifiers.ComputerSlugRegex
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class InstalledAppActionSource(
  private val callGraphStarter: CallGraphStarter,
  deployment: Deployment,
) : HttpActionSource {
  override val order: Int
    get() = 1

  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  private fun callGraph(userAgent: UserAgent) =
    callGraphStarter.start(userAgent)

  context(binder: HttpActionBinder)
  override fun bindActions() {
    val suffixRegex = Regex.escape(".${rootUrl.topPrivateDomain}")
    val appRegex = Regex("${AppSlugRegex.pattern}-${ComputerSlugRegex.pattern}$suffixRegex")

    binder.host(appRegex) {
      createAppRoutes()
    }
  }

  context(binder: HttpActionBinder)
  private fun createAppRoutes() {
    binder.routeAll {
      httpAction { userAgent, _, request ->
        val callGraph = callGraph(userAgent)
        callGraph.callAppAction.call(request)
      }
    }
  }
}
