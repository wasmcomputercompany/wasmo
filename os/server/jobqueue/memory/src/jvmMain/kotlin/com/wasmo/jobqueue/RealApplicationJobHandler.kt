package com.wasmo.jobqueue

import com.wasmo.db.WasmoDb
import com.wasmo.installedapps.InstalledAppStore
import com.wasmo.jobqueue.Job.ApplicationJob
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
class RealApplicationJobHandler(
  private val wasmoDb: WasmoDb,
  private val installedAppStore: InstalledAppStore,
) : JobStore.Handler<ApplicationJob> {

  context(scope: CoroutineScope)
  override suspend fun execute(job: ApplicationJob): Job? {
    val installedAppService = wasmoDb.transactionWithResult(noEnclosing = true) {
      installedAppStore.get(job.installedAppId)
    }

    if (installedAppService == null) return null
    val app = installedAppService.app() ?: return null
    val jobHandlerFactory = app.jobHandlerFactory ?: return null
    val jobHandler = jobHandlerFactory.get(job.queueName)

    return scope.launch {
      // TODO: handle dead letter
      jobHandler.handle(job.data)
    }
  }
}
