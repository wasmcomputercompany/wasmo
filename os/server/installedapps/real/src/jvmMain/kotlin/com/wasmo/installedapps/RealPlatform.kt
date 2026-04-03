package com.wasmo.installedapps

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import wasmo.app.Platform
import wasmo.downloader.Downloader
import wasmo.http.HttpService
import wasmo.jobs.JobQueue
import wasmo.objectstore.ObjectStore
import wasmo.sql.SqlService

@Inject
@SingleIn(InstalledAppScope::class)
class RealPlatform(
  override val clock: Clock,
  @ForInstalledApp override val httpService: HttpService,
  @ForInstalledApp override val objectStore: ObjectStore,
  @ForInstalledApp override val downloader: Downloader,
  @ForInstalledApp override val sqlService: SqlService,
  @ForInstalledApp override val jobQueueFactory: JobQueue.Factory,
) : Platform
