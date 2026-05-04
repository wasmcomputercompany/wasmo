package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.framework.UserAgent
import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(OsScope::class)
class CallGraphStarter(
  private val clientAuthenticatorFactory: ClientAuthenticator.Factory,
  private val callGraphFactory: CallGraph.Factory,
) {
  fun start(userAgent: UserAgent): CallGraph {
    val clientAuthenticator = clientAuthenticatorFactory.create(userAgent)
    clientAuthenticator.updateSessionCookie()
    return callGraphFactory.create(clientAuthenticator.get())
  }
}
