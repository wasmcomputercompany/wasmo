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
abstract class AccountsPasskeysBindings {
  companion object {
    @Provides
    @ElementsIntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
      hostnamePatterns: HostnamePatterns,
    ): List<ActionRegistration> = listOf(
      ActionRegistration.Rpc<AuthenticatePasskeyRequest, AuthenticatePasskeyResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/authenticate-passkey",
        ),
        action = AuthenticatePasskeyRpc::class,
      ),
      ActionRegistration.Rpc<RegisterPasskeyRequest, RegisterPasskeyResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/register-passkey",
        ),
        action = RegisterPasskeyRpc::class,
      ),
    )
  }
}
