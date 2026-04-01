package com.wasmo.installedapps

import com.wasmo.computers.ComputerStore
import com.wasmo.db.InstalledAppRelease
import com.wasmo.db.WasmoDb
import com.wasmo.events.EventListener
import com.wasmo.events.InstallAppEvent
import com.wasmo.issues.IssueCollector
import com.wasmo.jobs.JobExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@Inject
@SingleIn(AppScope::class)
class InstallAppJobExecutor(
  private val clock: Clock,
  private val wasmoDb: WasmoDb,
  private val computerStore: ComputerStore,
  private val eventListener: EventListener,
  private val installedAppStore: InstalledAppStore,
) : JobExecutor<InstallAppJob> {
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
      val release = wasmoDb.transactionWithResult(noEnclosing = true) {
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
        installedAppRelease = release,
      )

      service.app()?.afterInstall(0L, installedManifest.version)

      wasmoDb.transaction(noEnclosing = true) {
        val rowCount = wasmoDb.installedAppQueries.setRelease(
          id = installedApp.id,
          expected_version = installedApp.version,
          new_version = installedApp.version + 1L,
          active_release_id = release.id,
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
