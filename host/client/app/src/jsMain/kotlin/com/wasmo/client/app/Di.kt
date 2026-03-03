package com.wasmo.client.app

import com.wasmo.api.AccountSnapshot
import com.wasmo.api.RealWasmoApi
import com.wasmo.api.WasmoApi
import com.wasmo.api.WasmoJson
import com.wasmo.api.routes.RouteCodec
import com.wasmo.api.routes.RoutingContext
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.framework.PageData
import com.wasmo.framework.detectPageData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

fun createWasmoClientApp(
  logger: Logger = ConsoleLogger,
  environment: Environment,
): WasmoClientApp {
  val graph = createGraphFactory<WasmoClientAppGraph.Factory>()
    .create(logger, environment)
  return graph.wasmoClientApp
}

@DependencyGraph(AppScope::class)
interface WasmoClientAppGraph {
  val wasmoClientApp: WasmoClientApp

  @Binds
  fun provideWasmoApi(real: RealWasmoApi): WasmoApi

  @Binds
  fun provideRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @Provides
  @SingleIn(AppScope::class)
  fun provideScope(): CoroutineScope = MainScope()

  @Provides
  @SingleIn(AppScope::class)
  fun providePageData(): PageData = detectPageData(WasmoJson)

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
  fun provideRouteCodec(
    routingContext: RoutingContext,
    routeCodecFactory: RouteCodec.Factory,
  ): RouteCodec = routeCodecFactory.create(routingContext)

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides logger: Logger,
      @Provides environment: Environment,
    ): WasmoClientAppGraph
  }
}
