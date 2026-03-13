package com.wasmo.sql.r2dbc

import kotlinx.coroutines.channels.Channel
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class FakePublisher<T> : Publisher<T> {
  private lateinit var onlySubscriber: Subscriber<in T>
  private var complete = false
  val events = Channel<Event>(Channel.UNLIMITED)

  fun publish(value: T) {
    check(!complete)
    onlySubscriber.onNext(value)
  }

  fun acceptSubscription() {
    onlySubscriber.onSubscribe(
      object : Subscription {
        override fun request(n: Long) {
          require(n > 0L)
          events.trySend(Event.Request(n))
        }

        override fun cancel() {
          events.trySend(Event.CancelSubscription)
        }
      },
    )
  }

  fun complete() {
    check(::onlySubscriber.isInitialized)
    check(!complete)
    complete = true
    onlySubscriber.onComplete()
  }

  fun error(throwable: Throwable) {
    check(::onlySubscriber.isInitialized)
    check(!complete)
    complete = true
    onlySubscriber.onError(throwable)
  }

  override fun subscribe(subscriber: Subscriber<in T>) {
    check(!this::onlySubscriber.isInitialized)
    this.onlySubscriber = subscriber
  }

  sealed interface Event {
    data class Request(val count: Long) : Event
    data object CancelSubscription : Event
  }
}
