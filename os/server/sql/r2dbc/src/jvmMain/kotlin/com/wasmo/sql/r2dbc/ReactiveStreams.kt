package com.wasmo.sql.r2dbc

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/** Trigger a subscription without interest in its value. */
fun Publisher<Void>.subscribeAndDiscard() {
  return subscribe(
    object : Subscriber<Void> {
      override fun onSubscribe(s: Subscription) {
        s.request(Long.MAX_VALUE)
      }

      override fun onNext(t: Void?) {
      }

      override fun onError(t: Throwable?) {
      }

      override fun onComplete() {
      }
    },
  )
}
