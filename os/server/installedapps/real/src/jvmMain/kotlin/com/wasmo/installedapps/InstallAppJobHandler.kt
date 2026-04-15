package com.wasmo.installedapps

import com.wasmo.computers.ComputerStore
import com.wasmo.db.installedapps.InstalledAppRelease
import com.wasmo.db.installedapps.insertInstalledAppRelease
import com.wasmo.db.installedapps.selectInstalledAppById
import com.wasmo.db.installedapps.setRelease
import com.wasmo.events.EventListener
import com.wasmo.identifiers.OsScope
import com.wasmo.issues.IssueCollector
import com.wasmo.jobs.OsJobHandler
import com.wasmo.sql.transaction
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.sql.SqlDatabase

@Inject
@SingleIn(OsScope::class)
class InstallAppJobHandler(
  private val clock: Clock,
  private val wasmoDb: SqlDatabase,
  private val computerStore: ComputerStore,
  private val eventListener: EventListener,
  private val installedAppStore: InstalledAppStore,
) : OsJobHandler<InstallAppJob> {
  override suspend fun execute(job: InstallAppJob) {
    val (installedApp, computerService) = wasmoDb.transaction {
      val installedApp = selectInstalledAppById(job.installedAppId)
      installedApp to computerStore.get(installedApp.computer_id)
    }

    val installer = computerService.resourceInstallerFactory.create(
      appSlug = installedApp.slug,
      wasmoFileAddress = installedApp.wasmo_file_address,
    )

    val issueCollector = IssueCollector()
    val installedManifest = context(issueCollector) {
      installer.install()
    }

    val completedAt = clock.now()

    if (installedManifest != null) {
      val installedAppRelease = wasmoDb.transaction {
        val releaseId = insertInstalledAppRelease(
          first_active_at = completedAt,
          computer_id = computerService.id,
          installed_app_id = installedApp.id,
          app_version = installedManifest.version,
          app_manifest_data = installedManifest,
        )

        InstalledAppRelease(
          id = releaseId,
          first_active_at = completedAt,
          computer_id = computerService.id,
          installed_app_id = installedApp.id,
          app_version = installedManifest.version,
          app_manifest_data = installedManifest,
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
          new_version = installedApp.version + 1L,
          active_release_id = installedAppRelease.id,
          expected_version = installedApp.version,
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
