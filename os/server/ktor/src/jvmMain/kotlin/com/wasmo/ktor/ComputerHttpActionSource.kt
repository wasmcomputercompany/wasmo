package com.wasmo.ktor

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.deployment.Deployment
import com.wasmo.framework.HttpActionBinder
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.UserAgent
import com.wasmo.framework.decodeUrl
import com.wasmo.framework.rpc
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.ComputerSlugRegex
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class ComputerHttpActionSource(
  private val callGraphStarter: CallGraphStarter,
  deployment: Deployment,
) : HttpActionSource {
  override val order: Int
    get() = 0

  private val rootUrl = deployment.baseUrl.toString().decodeUrl()

  private fun callGraph(userAgent: UserAgent) =
    callGraphStarter.start(userAgent)

  context(binder: HttpActionBinder)
  override fun bindActions() {
    val suffixRegex = Regex.escape(".${rootUrl.topPrivateDomain}")
    val computerRegex = Regex("${ComputerSlugRegex.pattern}$suffixRegex")

    binder.host(computerRegex) {
      createComputerRoutes()

      // TODO: change the web app to always fetch static resources from the root URL.
      staticResources("/", "static")
    }
  }

  context(binder: HttpActionBinder)
  private fun createComputerRoutes() {
    binder.route("/") {
      httpAction { userAgent, url, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.osPage.get(url).response
      }
    }

    binder.rpc<InstallAppRequest, InstallAppResponse>(
      path = "/install-app",
    ) { userAgent, request, wasmoUrl ->
      val callGraph = callGraph(userAgent)
      callGraph.installAppRpc.install(
        computerSlug = ComputerSlug(wasmoUrl.subdomain ?: throw NotFoundUserException()),
        request = request,
      )
    }
  }
}
