package com.wasmo.testing.jobs

import com.wasmo.events.EventListener
import com.wasmo.identifiers.AppSlug
import com.wasmo.testing.events.HandleJobEvent
import okio.ByteString
import wasmo.jobs.JobHandler

/**
 * This just emits an event when a job is enqueued.
 */
class FakeJobHandler(
  val appSlug: AppSlug,
  val eventListener: EventListener,
  val queueName: String,
) : JobHandler {
  override suspend fun handle(job: ByteString) {
    eventListener.onEvent(
      HandleJobEvent(
        appSlug = appSlug,
        queueName = queueName,
        job = job,
      ),
    )
  }

  class Factory(
    val appSlug: AppSlug,
    val eventListener: EventListener,
  ) : JobHandler.Factory {
    override fun get(queueName: String) = FakeJobHandler(
      appSlug = appSlug,
      eventListener = eventListener,
      queueName = queueName,
    )
  }
}
