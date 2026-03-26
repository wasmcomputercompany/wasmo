package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.computers.packaging.Installer
import com.wasmo.db.WasmoDb
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerScope
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstallAppJobId
import com.wasmo.installedapps.InstalledAppStore
import com.wasmo.jobs.JobQueue
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
  private val installAppJobQueue: JobQueue<InstallAppJobId>,
  override val id: ComputerId,
  override val slug: ComputerSlug,
  override val installerFactory: Installer.Factory,
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
        slug = entry.slug,
      )
    }
  }

  context(transactionCallbacks: TransactionCallbacks)
  override fun enqueueInstall(
    appManifestAddress: AppManifestAddress,
    slug: AppSlug,
  ) {
    val installAppJobId = wasmoDb.installAppJobQueries.insertInstalledAppJob(
      computer_id = id,
      slug = slug,
      active = true,
      version = 1L,
      app_manifest_address = appManifestAddress,
      scheduled_at = clock.now(),
    ).executeAsOne()
    installAppJobQueue.enqueue(installAppJobId)
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
      apps = installedApps.map { installedApp ->
        installedAppStore.get(slug, installedApp).snapshot()
      },
    )
  }
}
