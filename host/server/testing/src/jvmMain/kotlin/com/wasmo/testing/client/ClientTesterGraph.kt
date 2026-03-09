package com.wasmo.testing.client

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.SessionCookie
import com.wasmo.testing.call.CallTesterGraph
import com.wasmo.testing.computer.ComputerTesterGraph
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(
  scope = ClientScope::class,
)
interface ClientTesterGraph {
  val clientTester: ClientTester
  val callTesterGraphFactory: CallTesterGraph.Factory
  val computerTesterGraphFactory: ComputerTesterGraph.Factory

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides clientAuthenticator: ClientAuthenticator,
      @Provides sessionCookie: SessionCookie,
    ): ClientTesterGraph
  }
}

abstract class ClientScope private constructor()
