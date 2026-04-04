package com.wasmo.testing.events

import com.wasmo.events.EventListener
import com.wasmo.identifiers.Event
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobCompletedEvent
import com.wasmo.jobs.JobEnqueuedEvent
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout

/**
 * Receives events from both production and test facets, and allows tests to consume those events.
 */
@Inject
@SingleIn(OsScope::class)
class TestEventListener : EventListener {
  @PublishedApi
  internal val events = MutableStateFlow<List<Event>>(listOf())

  override fun onEvent(event: Event) {
    events.update { it + event }
  }

  /**
   * Wait for the job queue to be idle, by waiting for the number of enqueued jobs to equal the
   * number of completed jobs.
   */
  suspend fun awaitIdle() {
    return withTimeout(1.seconds) {
      events.first { snapshot ->
        val enqueuedCount = snapshot.count { it is JobEnqueuedEvent }
        val completedCount = snapshot.count { it is JobCompletedEvent }
        println("enqueuedCount = $enqueuedCount, completedCount = $completedCount")
        enqueuedCount == completedCount
      }
    }
  }

  suspend inline fun <reified T : Event> receive(): T {
    return withTimeout(1.seconds) {
      while (true) {
        val snapshot = events.first { snapshot ->
          snapshot.any { it is T }
        }
        val index = snapshot.indexOfFirst { it is T }
        val result = snapshot[index] as T
        val update = snapshot.removeAt(index)
        if (!events.compareAndSet(snapshot, update)) continue
        return@withTimeout result
      }
      error("unreachable")
    }
  }

  fun receiveAll(): List<Event> {
    while (true) {
      val snapshot = events.value
      if (events.compareAndSet(snapshot, listOf())) {
        return snapshot
      }
    }
  }

  fun <T> List<T>.removeAt(index: Int): List<T> =
    slice(0 until index) + slice(index + 1 until size)
}
