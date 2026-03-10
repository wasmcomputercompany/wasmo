package com.wasmo.computers

import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.jobs.JobExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val installedAppId: InstalledAppId,
)

@Inject
@SingleIn(AppScope::class)
class InstallAppJobExecutor(
  private val wasmoDb: WasmoDb,
  private val computerStore: ComputerStore,
) : JobExecutor<InstallAppJob> {
  override suspend fun execute(job: InstallAppJob) {
    val installedApp = wasmoDb.transactionWithResult(noEnclosing = true) {
      val installedApp = wasmoDb.installedAppQueries.selectInstalledAppById(job.installedAppId)
        .executeAsOne()
      val computer = computerStore.get(installedApp.computer_id)
      computer.installedApp(installedApp)
    }

    installedApp.install()
  }
}
