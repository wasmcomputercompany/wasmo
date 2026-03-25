package com.wasmo.installedapps

import com.wasmo.db.WasmoDb
import com.wasmo.jobs.JobExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class InstallAppJobExecutor(
  private val wasmoDb: WasmoDb,
  private val installedAppStore: InstalledAppStore,
) : JobExecutor<InstallAppJob> {
  override suspend fun execute(job: InstallAppJob) {
//    val installedApp = wasmoDb.transactionWithResult(noEnclosing = true) {
//      val installedApp = wasmoDb.installedAppQueries.selectInstalledAppById(job.installedAppId)
//        .executeAsOne()
//      installedAppStore.get(installedApp)
//    }
//
//    installedApp.install()
  }
}
