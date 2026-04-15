package com.wasmo.computers

import com.wasmo.api.ComputerSnapshot
import com.wasmo.app.db.SqlTransaction
import com.wasmo.app.db.insertInstalledApp
import com.wasmo.app.db.selectInstalledAppsByComputerId
import com.wasmo.app.db.transactionWithResult
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerScope
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.installedapps.InstalledAppStore
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl
import wasmo.sql.SqlDatabase

@Inject
@SingleIn(ComputerScope::class)
class RealComputerService(
  private val deployment: Deployment,
  private val clock: Clock,
  private val wasmoDb: SqlDatabase,
  private val appCatalog: AppCatalog,
  private val jobQueue: OsJobQueue,
  private val installedAppStore: InstalledAppStore,
  override val id: ComputerId,
  override val slug: ComputerSlug,
  override val resourceInstallerFactory: ResourceInstaller.Factory,
) : ComputerService {
  override val url: HttpUrl
    get() = deployment.baseUrl.newBuilder()
      .host("$slug.${deployment.baseUrl.host}")
      .build()

  context(sqlTransaction: SqlTransaction)
  override suspend fun initialize() {
    for (entry in appCatalog.entries) {
      enqueueInstall(
        wasmoFileAddress = entry.wasmoFileAddress,
        slug = entry.slug,
      )
    }
  }

  context(sqlTransaction: SqlTransaction)
  override suspend fun enqueueInstall(
    wasmoFileAddress: WasmoFileAddress,
    slug: AppSlug,
  ) {
    val installedAppId = sqlTransaction.insertInstalledApp(
      installed_at = clock.now(),
      computer_id = id,
      slug = slug,
      active = true,
      version = 1L,
      wasmo_file_address = wasmoFileAddress,
    )
    sqlTransaction.afterCommit {
      jobQueue.enqueue(InstallAppJob(installedAppId))
    }
  }

  override suspend fun snapshot(): ComputerSnapshot {
    val installedApps = wasmoDb.transactionWithResult(noEnclosing = true) {
      contextOf<SqlTransaction>().selectInstalledAppsByComputerId(
        computer_id = id,
        active = true,
        limit = 100,
      )
    }

    val apps = installedApps.map { row ->
      val installedApp = installedAppStore.get(
        computerSlug = slug,
        installedApp = row.installedApp,
        installedAppRelease = row.installedAppRelease,
      )
      installedApp.snapshot()
    }

    return ComputerSnapshot(
      slug = slug,
      apps = apps,
    )
  }
}
