package com.wasmo.sql.r2dbc

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.wasmo.sql.r2dbc.FakePublisher.Event
import com.wasmo.testing.measureTestTime
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest

class PublisherIteratorTest {
  @Test
  fun publishSingleElement() = runTest {
    val publisher = FakePublisher<String>()

    val iterator = PublisherIterator(
      publisher = publisher,
      bufferSize = 0,
    )

    publisher.acceptSubscription()
    assertThat(publisher.events.tryReceive().isFailure).isTrue()

    val e0 = async {
      iterator.next()
    }
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
    publisher.publish("a")
    assertThat(e0.await()).isEqualTo("a")

    val e1 = async {
      iterator.next()
    }
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
    publisher.complete()
    assertThat(e1.await()).isNull()

    iterator.close()
    assertThat(publisher.events.receive()).isEqualTo(Event.CancelSubscription)
  }

  @Test
  fun bufferElements() = runTest {
    val publisher = FakePublisher<String>()

    val iterator = PublisherIterator(
      publisher = publisher,
      bufferSize = 3,
    )

    publisher.acceptSubscription()
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(3L))
    publisher.publish("a")
    publisher.publish("b")
    publisher.publish("c")

    assertThat(iterator.next()).isEqualTo("a")
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
    assertThat(iterator.next()).isEqualTo("b")
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
    assertThat(iterator.next()).isEqualTo("c")
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))

    iterator.close()
    assertThat(publisher.events.receive()).isEqualTo(Event.CancelSubscription)
  }

  @Test
  fun callNextBeforeSubscriptionIsAccepted() = runTest {
    val publisher = FakePublisher<String>()

    val iterator = PublisherIterator(
      publisher = publisher,
      bufferSize = 0,
    )

    val e0 = async {
      iterator.next()
    }

    launch {
      delay(5.seconds)
      publisher.acceptSubscription()
    }

    assertThat(
      measureTestTime {
        assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
      },
    ).isEqualTo(5.seconds)

    publisher.publish("a")
    assertThat(e0.await()).isEqualTo("a")

    iterator.close()
    assertThat(publisher.events.receive()).isEqualTo(Event.CancelSubscription)
  }

  @Test
  fun publishError() = runTest {
    val publisher = FakePublisher<String>()

    val iterator = PublisherIterator(
      publisher = publisher,
      bufferSize = 0,
    )

    publisher.acceptSubscription()

    supervisorScope {
      val e0 = async {
        iterator.next()
      }
      assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
      publisher.error(RuntimeException("boom"))
      assertFailsWith<RuntimeException> {
        e0.await()
      }
    }

    iterator.close()
    assertThat(publisher.events.receive()).isEqualTo(Event.CancelSubscription)
  }

  @Test
  fun closeBeforeSubscriptionAccepted() = runTest {
    val publisher = FakePublisher<String>()

    val iterator = PublisherIterator(
      publisher = publisher,
      bufferSize = 0,
    )
    iterator.close()

    assertFailsWith<IllegalStateException> {
      iterator.next()
    }

    iterator.close() // Idempotent.

    publisher.acceptSubscription()
    assertThat(publisher.events.receive()).isEqualTo(Event.CancelSubscription)
  }

  @Test
  fun terminalNextIsIdempotent() = runTest {
    val publisher = FakePublisher<String>()

    val iterator = PublisherIterator(
      publisher = publisher,
      bufferSize = 0,
    )

    publisher.acceptSubscription()
    publisher.complete()

    assertThat(iterator.next()).isNull()
    assertThat(iterator.next()).isNull()

    iterator.close()
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
    assertThat(publisher.events.receive()).isEqualTo(Event.Request(1L))
    assertThat(publisher.events.receive()).isEqualTo(Event.CancelSubscription)
  }
}
