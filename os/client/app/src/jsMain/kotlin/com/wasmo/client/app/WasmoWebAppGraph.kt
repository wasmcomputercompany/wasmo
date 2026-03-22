package com.wasmo.client.app

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.ComputerListSnapshot
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.RealWasmoApi
import com.wasmo.api.WasmoApi
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.client.app.browser.Browser
import com.wasmo.client.app.browser.RealBrowser
import com.wasmo.client.app.data.AccountDataService
import com.wasmo.client.app.data.RealAccountDataService
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.framework.PageData
import com.wasmo.passkeys.PasskeyAuthenticator
import com.wasmo.passkeys.RealPasskeyAuthenticator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@DependencyGraph(AppScope::class)
interface WasmoWebAppGraph {
  val wasmoWebApp: WasmoWebApp

  @Binds
  fun bindWasmoApi(real: RealWasmoApi): WasmoApi

  @Binds
  fun bindBrowser(real: RealBrowser): Browser

  @Binds
  fun bindRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @Binds
  fun bindPasskeyAuthenticator(real: RealPasskeyAuthenticator): PasskeyAuthenticator

  @Binds
  fun bindAccountDataService(real: RealAccountDataService): AccountDataService

  @Provides
  @SingleIn(AppScope::class)
  fun provideScope(): CoroutineScope = MainScope()

  @Provides
  @SingleIn(AppScope::class)
  fun provideStripePublishableKey(pageData: PageData): StripePublishableKey =
    pageData.get<StripePublishableKey>("stripe_publishable_key")
      ?: error("required stripe_publishable_key pageData not found")

  @Provides
  @SingleIn(AppScope::class)
  fun provideRoutingContext(pageData: PageData): RoutingContext =
    pageData.get<RoutingContext>("routing_context")
      ?: error("required routing_context pageData not found")

  @Provides
  @SingleIn(AppScope::class)
  fun provideAccountSnapshot(pageData: PageData): AccountSnapshot =
    pageData.get<AccountSnapshot>("account_snapshot")
      ?: error("required account_snapshot pageData not found")

  @Provides
  @SingleIn(AppScope::class)
  fun provideComputerSnapshot(pageData: PageData): ComputerSnapshot? =
    pageData.get<ComputerSnapshot>("computer_snapshot")

  @Provides
  @SingleIn(AppScope::class)
  fun provideComputerListSnapshot(pageData: PageData): ComputerListSnapshot? =
    pageData.get<ComputerListSnapshot>("computer_list_snapshot")

  @Provides
  @SingleIn(AppScope::class)
  fun provideRouteCodec(
    routingContext: RoutingContext,
    routeCodecFactory: RouteCodec.Factory,
  ): RouteCodec = routeCodecFactory.create(routingContext)

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides logger: Logger,
      @Provides environment: Environment,
      @Provides pageData: PageData,
    ): WasmoWebAppGraph
  }
}
