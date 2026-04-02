package com.wasmo.jobqueue

import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant

interface JobStore {
  fun enqueue(installedAppId: InstalledAppId, jobId: Long, executeAt: Instant?)
  fun cancel(installedAppId: InstalledAppId, jobId: Long)
}
