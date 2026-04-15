package com.wasmo.installedapps

import com.wasmo.sql.transaction
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.OsJobHandler
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import wasmo.sql.SqlDatabase

/**
 * TODO: This drops jobs if the app isn't currently runnable. For example, if the jobs are eligible
 *   for execution when the app is upgrading.
 */
@Inject
@SingleIn(OsScope::class)
class ApplicationJobHandler(
  private val wasmoDb: SqlDatabase,
  private val installedAppStore: InstalledAppStore,
) : OsJobHandler<ApplicationJob> {

  override suspend fun execute(job: ApplicationJob) {
    val installedAppService = wasmoDb.transaction {
      installedAppStore.get(job.installedAppId)
    }

    if (installedAppService == null) return
    val app = installedAppService.app() ?: return
    val jobHandlerFactory = app.jobHandlerFactory ?: return
    val jobHandler = jobHandlerFactory.get(job.queueName)

    // TODO: handle dead letter
    jobHandler.handle(job.data)
  }
}
