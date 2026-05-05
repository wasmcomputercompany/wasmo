package com.wasmo.computers

import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.rpc
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class ComputersActionSource(
  private val computersActionsFactory: ComputersActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: ActionSource.Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.osHostname) {
      rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
        path = "/create-computer-spec",
      ) { userAgent, request, _ ->
        val action = computersActionsFactory.create(userAgent).createComputerSpecRpc
        action.create(request)
      }
    }
  }
}
