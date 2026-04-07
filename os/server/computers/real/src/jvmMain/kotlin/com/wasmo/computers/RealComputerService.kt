package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.api.InstalledAppSnapshot
import com.wasmo.db.SelectInstalledAppsByComputerId
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerScope
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl

@Inject
@SingleIn(ComputerScope::class)
class RealComputerService(
  private val deployment: Deployment,
  private val clock: Clock,
  private val wasmoDb: WasmoDb,
  private val appCatalog: AppCatalog,
  private val jobQueue: OsJobQueue,
  override val id: ComputerId,
  override val slug: ComputerSlug,
  override val resourceInstallerFactory: ResourceInstaller.Factory,
) : ComputerService {
  override val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug.${deployment.baseUrl.host}")
      .build()

  context(transactionCallbacks: TransactionCallbacks)
  override fun initialize() {
    for (entry in appCatalog.entries) {
      enqueueInstall(
        wasmoFileAddress = entry.wasmoFileAddress,
        slug = entry.slug,
      )
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun enqueueInstall(
    wasmoFileAddress: WasmoFileAddress,
    slug: AppSlug,
  ) {
    val installedAppId = wasmoDb.installedAppQueries.insertInstalledApp(
      installed_at = clock.now(),
      computer_id = id,
      slug = slug,
      active = true,
      version = 1L,
      wasmo_file_address = wasmoFileAddress,
    ).executeAsOne()
    transactionCallbacks.afterCommit {
      jobQueue.enqueue(InstallAppJob(installedAppId))
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot(): ComputerSnapshot {
    val installedApps = wasmoDb.installedAppQueries.selectInstalledAppsByComputerId(
      computer_id = id,
      active = true,
      limit = 100,
    ).executeAsList()

    return ComputerSnapshot(
      slug = slug,
      apps = installedApps.map { row ->
        InstalledAppSnapshot(
          slug = row.slug,
          launcherLabel = row.app_manifest_data?.launcher?.label ?: row.slug.value,
          maskableIconUrl = row.maskableIconUrl.toString(),
          homeUrl = row.homeUrl.toString()
        )
      },
    )
  }

  private val SelectInstalledAppsByComputerId.appUrl: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug-${this@RealComputerService.slug}.${deployment.baseUrl.host}")
      .build()

  private val SelectInstalledAppsByComputerId.maskableIconUrl: HttpUrl
    get() = app_manifest_data?.launcher?.maskable_icon_path
      ?.let { appUrl.resolve(it) }
      ?: appUrl.resolve("/maskable-icon.svg")!!

  private val SelectInstalledAppsByComputerId.homeUrl: HttpUrl
    get() = app_manifest_data?.launcher?.home_path
      ?.let { appUrl.resolve(it) }
      ?: appUrl
}
