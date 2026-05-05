package com.wasmo.accounts.passkeys

import com.wasmo.api.AuthenticatePasskeyRequest
import com.wasmo.api.AuthenticatePasskeyResponse
import com.wasmo.api.RegisterPasskeyRequest
import com.wasmo.api.RegisterPasskeyResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.ActionSource
import com.wasmo.framework.ActionSource.Binder
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class PasskeysActionSource(
  private val passkeyActionsFactory: PasskeyActions.Factory,
  private val hostnamePatterns: HostnamePatterns,
) : ActionSource {
  override val order: Int
    get() = 0

  context(binder: Binder)
  override fun bindActions() {
    binder.register(
      ActionRegistration.Rpc<AuthenticatePasskeyRequest, AuthenticatePasskeyResponse>(
        HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/authenticate-passkey",
        ),
      ) { userAgent, request, _ ->
        val action = passkeyActionsFactory.create(userAgent).authenticatePasskeyRpc
        action.authenticate(request)
      },
    )

    binder.register(
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
}
