package com.wasmo.ktor

import com.wasmo.api.routes.RouteCodec
import com.wasmo.common.logging.Logger
import com.wasmo.common.routes.RealRouteCodec
import com.wasmo.computers.AppCatalog
import com.wasmo.computers.loadDefaultAppCatalogFromResources
import com.wasmo.events.EventListener
import com.wasmo.events.LoggingEventListener
import com.wasmo.framework.ContentTypeDatabase
import com.wasmo.framework.MDN
import com.wasmo.http.OkHttpClientHttpService
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.hostnamePatterns
import com.wasmo.journal.server.JournalWasmoApp
import com.wasmo.wasm.AppLoader
import com.wasmo.wasm.JvmAppLoader
import com.wasmo.website.RealServerOsHtml
import com.wasmo.website.ServerOsHtml
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.server.application.Application
import io.ktor.server.engine.EmbeddedServer
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Call
import okhttp3.Dns
import okhttp3.OkHttpClient
import okio.FileSystem
import wasmo.app.WasmoApp
import wasmo.http.HttpService

@BindingContainer
interface ServiceBindings {
  @Binds
  fun bindCallFactory(real: OkHttpClient): Call.Factory

  @Binds
  fun bindHttpClient(real: OkHttpClientHttpService): HttpService

  @Binds
  fun bindRouteCodecFactory(real: RealRouteCodec.Factory): RouteCodec.Factory

  @Binds
  fun bindServerOsHtmlFactory(real: RealServerOsHtml.Factory): ServerOsHtml.Factory

  @Binds
  fun bindEventListener(real: LoggingEventListener): EventListener

  @Binds
  fun bindLogger(real: KtorLogger): Logger

  @Binds
  fun bindAppLoader(real: JvmAppLoader): AppLoader

  companion object {

    @Provides
    @SingleIn(OsScope::class)
    fun provideClock(): Clock = Clock.System

    @Provides
    @SingleIn(OsScope::class)
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
      .dns(LocalhostSubdomainsDns(Dns.SYSTEM))
      .build()

    @Provides
    @SingleIn(OsScope::class)
    fun provideApplication(
      server: EmbeddedServer<*, *>,
    ): Application = server.application

    @Provides
    @SingleIn(OsScope::class)
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.Default)

    @Provides
    @SingleIn(OsScope::class)
    fun provideAppCatalog(): AppCatalog = loadDefaultAppCatalogFromResources()

    @Provides
    @SingleIn(OsScope::class)
    fun provideFileSystem(): FileSystem = FileSystem.SYSTEM

    @Provides
    @SingleIn(OsScope::class)
    fun provideWasmoAppFactories(): Map<AppSlug, WasmoApp.Factory> = mapOf(
      AppSlug("journal") to JournalWasmoApp.Factory(),
    )

    @Provides
    @SingleIn(OsScope::class)
    fun provideContentTypeDatabase(): ContentTypeDatabase = ContentTypeDatabase.MDN

    @Provides
    @SingleIn(OsScope::class)
    fun provideHostnamePatterns(deployment: Deployment): HostnamePatterns =
      deployment.hostnamePatterns()
  }
}
