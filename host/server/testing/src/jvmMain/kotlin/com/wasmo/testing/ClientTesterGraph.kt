package com.wasmo.testing

import com.wasmo.accounts.Challenger
import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.ClientScope
import com.wasmo.accounts.HmacChallenger
import com.wasmo.accounts.SessionCookie
import com.wasmo.calls.CallDataService
import com.wasmo.calls.RealCallDataService
import com.wasmo.computers.ComputerStore
import com.wasmo.computers.RealComputerStore
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@GraphExtension(
  scope = ClientScope::class,
)
interface ClientTesterGraph {
  val clientTester: ClientTester

  @Provides
  @SingleIn(ClientScope::class)
  fun provideChallenger(
    challengerFactory: HmacChallenger.Factory,
    sessionCookie: SessionCookie,
  ): Challenger = challengerFactory.create(sessionCookie.token)

  @Binds
  fun bindCallDataService(
    real: RealCallDataService,
  ): CallDataService

  @Binds
  fun bindComputerStore(
    real: RealComputerStore,
  ): ComputerStore

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides clientAuthenticator: ClientAuthenticator,
      @Provides sessionCookie: SessionCookie,
    ): ClientTesterGraph
  }
}
