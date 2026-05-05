package com.wasmo.computers

import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.framework.NotFoundUserException
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object ComputersActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    computersActionsFactory: ComputersActions.Factory,
    hostnamePatterns: HostnamePatterns,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/create-computer-spec",
      ),
    ) { userAgent, request, _ ->
      val action = computersActionsFactory.create(userAgent).createComputerSpecRpc
      action.create(request)
    },

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
