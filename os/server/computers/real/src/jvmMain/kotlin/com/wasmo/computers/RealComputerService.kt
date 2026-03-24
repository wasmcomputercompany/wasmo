package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.installedapps.InstalledAppStore
import com.wasmo.jobs.JobQueue
import com.wasmo.packaging.AppManifest
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
  private val installedAppStore: InstalledAppStore,
  private val installAppJobQueue: JobQueue<InstallAppJob>,
  override val id: ComputerId,
  override val slug: ComputerSlug,
  override val manifestLoader: ManifestLoader,
) : ComputerService {
  override val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug.${deployment.baseUrl.host}")
      .build()

  context(transactionCallbacks: TransactionCallbacks)
  override fun initialize() {
    for (entry in appCatalog.entries) {
      enqueueInstall(
        appManifestAddress = entry.appManifestAddress,
        appManifest = entry.manifest,
      )
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun enqueueInstall(
    appManifestAddress: AppManifestAddress,
    appManifest: AppManifest,
  ) {
    val installedAppId = wasmoDb.installedAppQueries.insertInstalledApp(
      computer_id = id,
      slug = AppSlug(appManifest.slug),
      manifest_address = appManifestAddress.toString(),
      manifest_data = appManifest,
      version = appManifest.version,
      install_scheduled_at = clock.now(),
    ).executeAsOne()

    installAppJobQueue.enqueue(InstallAppJob(installedAppId))
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun snapshot(): ComputerSnapshot {
    val installedApps = wasmoDb.installedAppQueries.selectInstalledAppsByComputerId(
      computer_id = id,
      limit = 100,
    ).executeAsList()

    return ComputerSnapshot(
      slug = slug,
      apps = installedApps.map { installedApp ->
        installedAppStore.get(slug, installedApp).snapshot()
      },
    )
  }
}
