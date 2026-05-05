package com.wasmo.computers

import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.ActionSource
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.NotFoundUserException
import com.wasmo.identifiers.ComputerSlug
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
    binder.register(
      ActionRegistration.Rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
        HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/create-computer-spec",
        ),
      ) { userAgent, request, _ ->
        val action = computersActionsFactory.create(userAgent).createComputerSpecRpc
        action.create(request)
      },
    )

    binder.register(
      ActionRegistration.Rpc<InstallAppRequest, InstallAppResponse>(
        HttpRequestPattern(
          host = hostnamePatterns.computerRegex,
          path = "/install-app",
        ),
      ) { userAgent, request, wasmoUrl ->
        val action = computersActionsFactory.create(userAgent).installAppRpc
        action.install(
          computerSlug = ComputerSlug(wasmoUrl.subdomain ?: throw NotFoundUserException()),
          request = request,
        )
      },
    )
  }
}
