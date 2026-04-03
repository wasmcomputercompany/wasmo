package com.wasmo.jobqueue

import com.wasmo.db.WasmoDb
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.installedapps.InstalledAppStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.ByteString

@Inject
@SingleIn(AppScope::class)
class RealJobStoreHandler(
  private val wasmoDb: WasmoDb,
  private val installedAppStore: InstalledAppStore,
) : JobStore.Handler {
  context(scope: CoroutineScope)
  override suspend fun execute(
    installedAppId: InstalledAppId,
    queueName: String,
    job: ByteString,
  ): Job? {
    val installedAppService = wasmoDb.transactionWithResult(noEnclosing = true) {
      installedAppStore.get(installedAppId)
    }

    if (installedAppService == null) return null
    val app = installedAppService.app() ?: return null
    val jobHandlerFactory = app.jobHandlerFactory ?: return null
    val jobHandler = jobHandlerFactory.get(queueName)

    return scope.launch {
      // TODO: handle dead letter
      jobHandler.handle(job)
    }
  }
}
