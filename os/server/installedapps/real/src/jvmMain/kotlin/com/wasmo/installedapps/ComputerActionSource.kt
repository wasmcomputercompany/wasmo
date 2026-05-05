package com.wasmo.installedapps

import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.rpc
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class ComputerActionSource(
  private val computerActionsFactory: ComputerActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: Binder)
  override fun bindActions() {
    binder.host(hostnamePatterns.computerRegex) {
      rpc<InstallAppRequest, InstallAppResponse>(
        path = "/install-app",
      ) { userAgent, request, wasmoUrl ->
        val action = computerActionsFactory.create(userAgent).installAppRpc
        action.install(
          computerSlug = ComputerSlug(wasmoUrl.subdomain ?: throw NotFoundUserException()),
          request = request,
        )
      }
    }
  }
}
