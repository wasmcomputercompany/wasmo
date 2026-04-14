package com.wasmo.installedapps

import com.wasmo.computers.ComputerStore
import com.wasmo.app.db.InstalledAppRelease
import com.wasmo.app.db.WasmoDb
import com.wasmo.events.EventListener
import com.wasmo.identifiers.OsScope
import com.wasmo.issues.IssueCollector
import com.wasmo.jobs.OsJobHandler
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@Inject
@SingleIn(OsScope::class)
class InstallAppJobHandler(
  private val clock: Clock,
  private val wasmoDb: WasmoDb,
  private val computerStore: ComputerStore,
  private val eventListener: EventListener,
  private val installedAppStore: InstalledAppStore,
) : OsJobHandler<InstallAppJob> {
  override suspend fun execute(job: InstallAppJob) {
    val (installedApp, computerService) = wasmoDb.transactionWithResult(noEnclosing = true) {
      val installedApp = wasmoDb.installedAppQueries.selectInstalledAppById(job.installedAppId)
        .executeAsOne()
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
      val installedAppRelease = wasmoDb.transactionWithResult(noEnclosing = true) {
        val releaseId = wasmoDb.installedAppReleaseQueries.insertInstalledAppRelease(
          first_active_at = completedAt,
          computer_id = computerService.id,
          installed_app_id = installedApp.id,
          app_version = installedManifest.version,
          app_manifest_data = installedManifest,
        ).executeAsOne()

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

      wasmoDb.transaction(noEnclosing = true) {
        val rowCount = wasmoDb.installedAppQueries.setRelease(
          id = installedApp.id,
          expected_version = installedApp.version,
          new_version = installedApp.version + 1L,
          active_release_id = installedAppRelease.id,
        ).value
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
