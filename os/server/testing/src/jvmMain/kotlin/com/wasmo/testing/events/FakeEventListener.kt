package com.wasmo.testing.events

import com.wasmo.events.Event
import com.wasmo.events.EventListener
import com.wasmo.testing.JobQueueTester
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout

/**
 * Receives events from both production and test facets, and allows tests to consume those events.
 */
@Inject
@SingleIn(AppScope::class)
class FakeEventListener(
  private val lazyJobQueueTester: Lazy<JobQueueTester>,
) : EventListener {
  private val channel = Channel<Event>(capacity = Int.MAX_VALUE)

  override fun onEvent(event: Event) {
    val trySend = channel.trySend(event)
    check(trySend.isSuccess)
  }

  suspend inline fun <reified T : Event> receive(): T {
    while (true) {
      val candidate = receiveInternal()
      if (candidate is T) return candidate
    }
  }

  @PublishedApi
  internal suspend fun receiveInternal(): Event {
    return withTimeout(1.seconds) {
      channel.receive()
    }
  }

  suspend fun receiveAll(): List<Event> {
    lazyJobQueueTester.value.awaitIdle()
    return buildList {
      while (true) {
        val result = channel.tryReceive()
        if (!result.isSuccess) break
        add(result.getOrThrow())
      }
    }
  }
}
