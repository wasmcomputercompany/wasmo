package com.wasmo.testing.installedapp

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.installedapps.InstalledAppService
import com.wasmo.installedapps.InstalledAppStore
import com.wasmo.testing.apps.PublishedApp
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import okhttp3.HttpUrl
import wasmo.app.WasmoApp

/**
 * Tests an app installed on a specific computer.
 */
@AssistedInject
class InstalledAppTester private constructor(
  private val deployment: Deployment,
  private val wasmoDb: WasmoDb,
  private val appLoader: AppLoader,
  private val installedAppStore: InstalledAppStore,
  @Assisted private val clientAuthenticator: ClientAuthenticator,
  @Assisted val publishedApp: PublishedApp,
  @Assisted val computerSlug: ComputerSlug,
) {
  val slug: AppSlug
    get() = publishedApp.slug

  val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("${publishedApp.slug}-$computerSlug.${deployment.baseUrl.host}")
      .build()
  val iconUrl: HttpUrl
    get() = url.resolve("/maskable-icon.svg")!!

  suspend fun load(): WasmoApp {
    val installedAppService = installedAppService()
    return appLoader.load(installedAppService.platform, publishedApp.slug)
      ?: error("failed to load ${installedAppService.slug}")
  }

  private fun installedAppService(): InstalledAppService {
    val client = clientAuthenticator.get()
    return wasmoDb.transactionWithResult(noEnclosing = true) {
      installedAppStore.getOrNull(client, computerSlug, publishedApp.slug)!!
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      clientAuthenticator: ClientAuthenticator,
      publishedApp: PublishedApp,
      computerSlug: ComputerSlug,
    ): InstalledAppTester
  }
}
