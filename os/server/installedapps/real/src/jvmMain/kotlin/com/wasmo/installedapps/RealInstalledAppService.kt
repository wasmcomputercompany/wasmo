package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.InstallIncompleteReason
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.db.InstalledApp
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.packaging.AppManifest
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl
import wasmo.app.Platform

@Inject
@SingleIn(InstalledAppScope::class)
class RealInstalledAppService(
  private val deployment: Deployment,
  private val computerSlug: ComputerSlug,
  private val installedApp: InstalledApp,
  private val appInstaller: AppInstaller,
  override val slug: AppSlug,
  override val manifest: AppManifest,
  override val httpService: InstalledAppHttpService,
  override val platform: Platform,
) : InstalledAppService {
  override val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug-$computerSlug.${deployment.baseUrl.host}")
      .build()

  override val maskableIconUrl: HttpUrl
    get() = manifest.launcher?.maskable_icon_path
      ?.let { url.resolve(it) }
      ?: url.resolve("/maskable-icon.svg")!!

  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot() = InstalledAppSnapshot(
    slug = slug,
    launcherLabel = manifest.launcher?.label ?: slug.value,
    maskableIconUrl = maskableIconUrl.toString(),
    installScheduledAt = installedApp.install_scheduled_at,
    installCompletedAt = installedApp.install_completed_at,
    installDeletedAt = installedApp.install_deleted_at,
    installIncompleteReason = installedApp.install_incomplete_reason
      ?.let { InstallIncompleteReason.valueOf(it) },
  )

  override suspend fun install() {
    appInstaller.install()
  }
}
