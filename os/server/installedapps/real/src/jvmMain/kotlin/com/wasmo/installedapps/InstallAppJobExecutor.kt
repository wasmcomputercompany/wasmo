package com.wasmo.installedapps

import com.wasmo.computers.ComputerStore
import com.wasmo.db.WasmoDb
import com.wasmo.events.EventListener
import com.wasmo.events.InstallAppEvent
import com.wasmo.identifiers.InstallAppJobId
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
) : JobExecutor<InstallAppJobId> {
  override suspend fun execute(job: InstallAppJobId) {
    val (installAppJob, computerService) = wasmoDb.transactionWithResult(noEnclosing = true) {
      val installAppJob = wasmoDb.installAppJobQueries.selectInstallAppJobById(job)
        .executeAsOne()
      installAppJob to computerStore.get(installAppJob.computer_id)
    }

    val installer = computerService.resourceInstallerFactory.create(
      appSlug = installAppJob.slug,
      wasmoFileAddress = installAppJob.wasmo_file_address,
    )

    val issueCollector = IssueCollector()
    val installedManifest = context(issueCollector) {
      installer.install()
    }

    val completedAt = clock.now()

    wasmoDb.transaction(noEnclosing = true) {
      if (installedManifest != null) {
        val installedAppId = wasmoDb.installedAppQueries.insertInstalledApp(
          computer_id = computerService.id,
          slug = installAppJob.slug,
          active = true,
          version = 1L,
          wasmo_file_address = installAppJob.wasmo_file_address,
          manifest_data = installedManifest,
        ).executeAsOne()
        val rowCount = wasmoDb.installAppJobQueries.updateInstallAppJobSetCompletedAt(
          id = installAppJob.id,
          expected_version = installAppJob.version,
          new_version = installAppJob.version + 1L,
          completed_at = completedAt,
          active = false,
          installed_app_id = installedAppId,
        ).value
        require(rowCount == 1L)

      } else {
        val rowCount = wasmoDb.installAppJobQueries.updateInstallAppJobSetIncompleteReason(
          id = installAppJob.id,
          expected_version = installAppJob.version,
          new_version = installAppJob.version + 1L,
          incomplete_reason = issueCollector.issues.firstOrNull()?.toString(),
          active = false,
        ).value
        require(rowCount == 1L)
      }
    }

    eventListener.onEvent(
      InstallAppEvent(
        appSlug = installAppJob.slug,
        computerSlug = computerService.slug,
        issues = issueCollector.issues.toList(),
      ),
    )
  }
}
