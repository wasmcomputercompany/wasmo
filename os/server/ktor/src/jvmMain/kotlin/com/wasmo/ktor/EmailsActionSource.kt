package com.wasmo.ktor

import com.wasmo.api.ConfirmEmailAddressRequest
import com.wasmo.api.ConfirmEmailAddressResponse
import com.wasmo.api.LinkEmailAddressRequest
import com.wasmo.api.LinkEmailAddressResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.UserAgent
import com.wasmo.framework.rpc
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class EmailsActionSource(
  private val callGraphStarter: CallGraphStarter,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 3

  private fun callGraph(userAgent: UserAgent) =
    callGraphStarter.start(userAgent)

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.osHostname) {
      rpc<ConfirmEmailAddressRequest, ConfirmEmailAddressResponse>(
        path = "/confirm-email-address",
      ) { userAgent, request, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.confirmEmailAddressRpc.confirm(request)
      }

      rpc<LinkEmailAddressRequest, LinkEmailAddressResponse>(
        path = "/link-email-address",
      ) { userAgent, request, _ ->
        val callGraph = callGraph(userAgent)
        callGraph.linkEmailAddressRpc.link(request)
      }
    }
  }
}
