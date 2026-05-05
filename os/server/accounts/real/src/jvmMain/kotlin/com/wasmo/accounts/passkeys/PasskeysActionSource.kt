package com.wasmo.accounts.passkeys

import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
object PasskeysActionSource {
  @Provides
  @ElementsIntoSet
  @SingleIn(OsScope::class)
  fun provideActionRegistrations(
    passkeyActionsFactory: PasskeyActions.Factory,
    hostnamePatterns: HostnamePatterns,
  ): List<ActionRegistration> = listOf(
    ActionRegistration.Rpc<AuthenticatePasskeyRequest, AuthenticatePasskeyResponse>(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/authenticate-passkey",
      ),
    ) { userAgent, request, _ ->
      val action = passkeyActionsFactory.create(userAgent).authenticatePasskeyRpc
      action.authenticate(request)
    },
    ActionRegistration.Rpc<RegisterPasskeyRequest, RegisterPasskeyResponse>(
      HttpRequestPattern(
        host = hostnamePatterns.osHostname,
        path = "/register-passkey",
      ),
    ) { userAgent, request, _ ->
      val action = passkeyActionsFactory.create(userAgent).registerPasskeyRpc
      action.register(request)
    },
  )
}
