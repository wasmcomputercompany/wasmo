package com.wasmo.sql.r2dbc

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Adapt a callback-driven [Publisher] to a suspending iterator.
 *
 * Our channel has capacity for up to 2 extra elements, to support an element added immediately
 * after the call to [Subscription.request] plus the tombstone null element. Without this extra 2
 * using [Channel.trySend] could fail because the channel is full.
 */
internal class PublisherIterator<T : Any>(
  publisher: Publisher<T>,
  private val bufferSize: Int = 0,
) {
  /** Null if this was closed before the subscription was received. */
  private val subscriptionDeferred = CompletableDeferred<Subscription?>()
  private var channel = Channel<T?>(
    capacity = bufferSize.coerceAtMost(Int.MAX_VALUE - 2) + 2,
  )

  init {
    require(bufferSize >= 0)
    publisher.subscribe(
      object : Subscriber<T> {
        override fun onSubscribe(subscription: Subscription) {
          val bufferRequest = bufferSize.coerceAtMost(Int.MAX_VALUE - 2).toLong()
          if (bufferRequest > 0) {
            subscription.request(bufferRequest)
          }
          if (!subscriptionDeferred.complete(subscription)) {
            subscription.cancel()
          }
        }

        override fun onNext(postgresqlResult: T) {
          val channelResult = channel.trySend(postgresqlResult)
          check(channelResult.isSuccess)
        }

        override fun onError(throwable: Throwable) {
          channel.close(throwable)
        }

        override fun onComplete() {
          val channelResult = channel.trySend(null)
          check(channelResult.isSuccess)
        }
      },
    )
  }

  suspend fun next(): T? {
    val subscription = subscriptionDeferred.await()
    check(subscription != null) { "closed" }

    subscription.request(1L)

    val element = channel.receive()

    // If we read out our tombstone element, replace it because the terminal next() is idempotent.
    if (element == null) {
      channel.trySend(null)
      return null
    }

    return element
  }

  fun close() {
    if (!subscriptionDeferred.complete(null)) {
      subscriptionDeferred.getCompleted()?.cancel()
    }
  }
}
