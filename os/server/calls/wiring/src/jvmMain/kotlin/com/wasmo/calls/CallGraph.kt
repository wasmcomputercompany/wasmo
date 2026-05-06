package com.wasmo.calls

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.Client
import com.wasmo.framework.HttpAction
import com.wasmo.framework.RpcAction
import com.wasmo.passkeys.PasskeyChecker
import com.wasmo.passkeys.RealPasskeyChecker
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

@GraphExtension(
  scope = CallScope::class,
)
interface CallGraph {
  val rpcActions: Map<KClass<*>, Provider<RpcAction<*, *>>>
  val httpActions: Map<KClass<*>, Provider<HttpAction>>

  @Provides
  @SingleIn(CallScope::class)
  fun provideChallenger(
    client: Client,
  ): Challenger = client.challenger

  @Binds
  fun bindPasskeyChecker(
    real: RealPasskeyChecker,
  ): PasskeyChecker

  @Binds
  fun bindCallDataService(
    real: RealCallDataService,
  ): CallDataService

  @GraphExtension.Factory
  interface Factory {
    fun create(@Provides client: Client): CallGraph
  }
}
