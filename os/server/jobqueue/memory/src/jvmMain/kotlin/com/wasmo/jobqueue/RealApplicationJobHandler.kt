package com.wasmo.jobqueue

import com.wasmo.db.WasmoDb
import com.wasmo.installedapps.ApplicationJob
import com.wasmo.installedapps.InstalledAppStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class RealApplicationJobHandler(
  private val wasmoDb: WasmoDb,
  private val installedAppStore: InstalledAppStore,
) : JobStore.Handler<ApplicationJob> {

  override suspend fun execute(job: ApplicationJob) {
    val installedAppService = wasmoDb.transactionWithResult(noEnclosing = true) {
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
