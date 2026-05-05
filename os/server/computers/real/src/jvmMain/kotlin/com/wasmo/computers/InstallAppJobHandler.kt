package com.wasmo.computers

import com.wasmo.db.installedapps.DbInstalledAppRelease
import com.wasmo.db.installedapps.insertInstalledAppRelease
import com.wasmo.db.installedapps.selectInstalledAppById
import com.wasmo.db.installedapps.setRelease
import com.wasmo.events.EventListener
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstallAppEvent
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.installedapps.InstalledAppStore
import com.wasmo.issues.IssueCollector
import com.wasmo.jobs.OsJobHandler
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase
import wasmox.sql.transaction

@Inject
@SingleIn(OsScope::class)
class InstallAppJobHandler(
  private val clock: Clock,
  private val wasmoDb: SqlDatabase,
  private val computerStore: ComputerStore,
  private val eventListener: EventListener,
  private val installedAppStore: InstalledAppStore,
) : OsJobHandler<InstallAppJob, Unit> {
  context(context: OsJobHandler.Context)
  override suspend fun handle(job: InstallAppJob) {
    val (installedApp, computerService) = wasmoDb.transaction {
      val installedApp = selectInstalledAppById(job.installedAppId)
      installedApp to computerStore.get(installedApp.computerId)
    }

    val installer = computerService.resourceInstallerFactory.create(
      appSlug = installedApp.slug,
      wasmoFileAddress = installedApp.wasmoFileAddress,
    )

    val issueCollector = IssueCollector()
    val installedManifest = context(issueCollector) {
      installer.install()
    }

    val completedAt = clock.now()

    if (installedManifest != null) {
      val installedAppRelease = wasmoDb.transaction {
        val releaseId = insertInstalledAppRelease(
          firstActiveAt = completedAt,
          computerId = computerService.id,
          installedAppId = installedApp.id,
          appVersion = installedManifest.version,
          appManifestData = installedManifest,
        )

        DbInstalledAppRelease(
          id = releaseId,
          firstActiveAt = completedAt,
          computerId = computerService.id,
          installedAppId = installedApp.id,
          appVersion = installedManifest.version,
          appManifestData = installedManifest,
        )
      }

      val service = installedAppStore.get(
        computerSlug = computerService.slug,
        installedApp = installedApp,
        installedAppRelease = installedAppRelease,
      )

      service.app()?.afterInstall(0L, installedManifest.version)

      wasmoDb.transaction {
        val rowCount = setRelease(
          newVersion = installedApp.version + 1L,
          activeReleaseId = installedAppRelease.id,
          expectedVersion = installedApp.version,
          id = installedApp.id,
        )
        require(rowCount == 1L)
      }
    } else {
      // TODO: Retry later?
    }

    eventListener.onEvent(
      InstallAppEvent(
        appSlug = installedApp.slug,
        computerSlug = computerService.slug,
        issues = issueCollector.issues.toList(),
      ),
    )
  }
}
