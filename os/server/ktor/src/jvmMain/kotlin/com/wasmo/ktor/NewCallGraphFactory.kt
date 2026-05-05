package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.passkeys.PasskeyActions
import com.wasmo.framework.UserAgent
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class NewCallGraphFactory(
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val callGraphFactory: CallGraph.Factory,
) : PasskeyActions.Factory {
  override fun create(userAgent: UserAgent): CallGraph {
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    clientAuthenticator.updateSessionCookie()
    return callGraphFactory.create(clientAuthenticator.get())
  }
}
