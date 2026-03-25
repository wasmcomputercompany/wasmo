package com.wasmo.installedapps

import com.wasmo.computers.ComputerStore
import com.wasmo.db.WasmoDb
import com.wasmo.issues.IssueCollector
import com.wasmo.jobs.JobExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class InstallAppJobExecutor(
  private val wasmoDb: WasmoDb,
  private val computerStore: ComputerStore,
) : JobExecutor<InstallAppJob> {
  override suspend fun execute(job: InstallAppJob) {

    val computerService = wasmoDb.transactionWithResult(noEnclosing = true) {
      computerStore.get(job.computerId)
    }
    val installer = computerService.installerFactory.create(job.appManifestAddress)

    val issueCollector = IssueCollector()
    val installedManifest = context(issueCollector) {
      installer.install()
    }

    // If success, insert an installed app
    // Always update the install attempt

//    // TODO: insertInstalledApp
//    if (issueCollector.issues.isEmpty()) {
//      wasmoDb.transaction(noEnclosing = true) {
//        if (issueCollector.issues.isEmpty()) {
//          val rowCount = wasmoDb.installedAppQueries.updateInstalledAppSetInstallCompletedAt(
//            id = installedApp.id,
//            expected_version = installedApp.version,
//            new_version = installedApp.version + 1L,
//            install_completed_at = clock.now(),
//          ).value
//          require(rowCount == 1L)
//        } else {
//          val rowCount = wasmoDb.installedAppQueries.updateInstalledAppSetInstallIncompleteReason(
//            id = installedApp.id,
//            expected_version = installedApp.version,
//            new_version = installedApp.version + 1L,
//            install_incomplete_reason = installResult.reason.name,
//          ).value
//          require(rowCount == 1L)
//        }
//      }
//    }
//    eventListener.onEvent(
//      InstallAppEvent(
//        appSlug = installedApp.slug,
//        computerSlug = computerSlug,
//        exception = (installResult as? ResourceResult.Failed)?.exception,
//      ),
//    )
  }
}
