package com.wasmo.testing

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.SessionCookie
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(
  scope = ClientScope::class,
)
interface ClientTesterGraph {
  val clientTester: ClientTester
  val callTesterGraphFactory: CallTesterGraph.Factory

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides clientAuthenticator: ClientAuthenticator,
      @Provides sessionCookie: SessionCookie,
    ): ClientTesterGraph
  }
}

abstract class ClientScope private constructor()
