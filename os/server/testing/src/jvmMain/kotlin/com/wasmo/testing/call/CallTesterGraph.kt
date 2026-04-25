package com.wasmo.testing.call

import com.wasmo.accounts.CallScope
import com.wasmo.accounts.Challenger
import com.wasmo.accounts.Client
import com.wasmo.accounts.HmacChallenger
import com.wasmo.accounts.SessionCookie
import com.wasmo.api.routes.RouteCodec
import com.wasmo.calls.CallDataService
import com.wasmo.calls.RealCallDataService
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.passkeys.PasskeyChecker
import com.wasmo.passkeys.RealPasskeyChecker
import com.wasmo.website.RealServerOsHtml
import com.wasmo.website.ServerOsHtml
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@GraphExtension(
  scope = CallScope::class,
)
interface CallTesterGraph {
  val callTester: CallTester

  @Provides
  @SingleIn(CallScope::class)
  fun provideChallenger(
    challengerFactory: HmacChallenger.Factory,
    sessionCookie: SessionCookie,
  ): Challenger = challengerFactory.create(sessionCookie.token)

  @Binds
  fun bindCallDataService(real: RealCallDataService): CallDataService

  @Binds
  fun bindPasskeyChecker(real: RealPasskeyChecker): PasskeyChecker

  @Binds
  fun bindServerOsHtmlFactory(real: RealServerOsHtml.Factory): ServerOsHtml.Factory

  @Binds
  fun bindRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides client: Client,
      @Provides sessionCookie: SessionCookie,
    ): CallTesterGraph
  }
}
