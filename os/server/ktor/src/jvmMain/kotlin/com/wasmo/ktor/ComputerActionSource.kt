package com.wasmo.ktor

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.UserAgent
import com.wasmo.framework.rpc
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class ComputerActionSource(
  private val callGraphStarter: CallGraphStarter,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  private fun callGraph(userAgent: UserAgent) =
    callGraphStarter.start(userAgent)

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.computerRegex) {
      route("/") {
        httpAction { userAgent, url, _ ->
          val callGraph = callGraph(userAgent)
          callGraph.osPage.get(url).response
        }
      }

      rpc<InstallAppRequest, InstallAppResponse>(
        path = "/install-app",
      ) { userAgent, request, wasmoUrl ->
        val callGraph = callGraph(userAgent)
        callGraph.installAppRpc.install(
          computerSlug = ComputerSlug(wasmoUrl.subdomain ?: throw NotFoundUserException()),
          request = request,
        )
      }

      // TODO: change the web app to always fetch static resources from the root URL.
      staticResources("/", "static")
    }
  }
}
