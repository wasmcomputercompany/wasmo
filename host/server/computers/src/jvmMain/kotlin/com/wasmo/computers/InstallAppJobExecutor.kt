package com.wasmo.computers

import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.AppInstallId
import com.wasmo.jobs.JobExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppJob(
  val appInstallId: AppInstallId,
)

@Inject
@SingleIn(AppScope::class)
class InstallAppJobExecutor(
  private val wasmoDb: WasmoDb,
  private val manifestLoader: ManifestLoader,
  private val computerStore: ComputerStore,
) : JobExecutor<InstallAppJob> {
  override suspend fun execute(job: InstallAppJob) {
    val (appInstall, computer) = wasmoDb.transactionWithResult(noEnclosing = true) {
      val appInstall = wasmoDb.appInstallQueries.selectAppInstallById(job.appInstallId)
        .executeAsOne()
      val computer = computerStore.get(appInstall.computer_id)
      appInstall to computer
    }

    val manifest = manifestLoader.loadManifest(appInstall.manifest_url)
    computer.appLoader.downloadWasm(manifest)
  }
}
