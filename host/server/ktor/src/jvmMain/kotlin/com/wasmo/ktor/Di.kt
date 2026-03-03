package com.wasmo.ktor

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.routes.RouteCodec
import com.wasmo.app.db.WasmoDbService
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.deployment.Deployment
import com.wasmo.http.HttpClient
import com.wasmo.http.RealHttpClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.EngineMain
import kotlin.time.Clock
import okhttp3.Call
import okhttp3.OkHttpClient

fun startWasmoService(
  config: WasmoServiceConfig,
  args: Array<String>,
): WasmoService {
  val server = EngineMain.createServer(args)

  val wasmoDbService = WasmoDbService.start(
    hostname = config.postgresDatabaseHostname,
    databaseName = config.postgresDatabaseName,
    user = config.postgresDatabaseUser,
    password = config.postgresDatabasePassword,
    ssl = false,
  )

  val graphFactory = createGraphFactory<WasmoServiceGraph.Factory>()
  val graph = graphFactory.create(
    config = config,
    server = server,
    wasmoDbService = wasmoDbService,
  )

  graph.wasmoService.start()
  return graph.wasmoService
}

@DependencyGraph(AppScope::class)
interface WasmoServiceGraph {
  val wasmoService: WasmoService

  @Provides
  @SingleIn(AppScope::class)
  fun provideClock(): Clock = Clock.System

  @Provides
  @SingleIn(AppScope::class)
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

  @Provides
  @SingleIn(AppScope::class)
  fun provideCookieSecret(config: WasmoServiceConfig): CookieSecret =
    CookieSecret(config.cookieSecret)

  @Provides
  @SingleIn(AppScope::class)
  fun provideDeployment(config: WasmoServiceConfig): Deployment =
    config.deployment

  @Provides
  @SingleIn(AppScope::class)
  fun provideSessionCookieSpec(config: WasmoServiceConfig): SessionCookieSpec =
    config.sessionCookieSpec

  @Binds
  fun bindCallFactory(real: OkHttpClient): Call.Factory

  @Binds
  fun bindHttpClient(real: RealHttpClient): HttpClient

  @Binds
  fun bindRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @Binds
  fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides config: WasmoServiceConfig,
      @Provides server: EmbeddedServer<*, *>,
      @Provides wasmoDbService: WasmoDbService,
    ): WasmoServiceGraph
  }
}
