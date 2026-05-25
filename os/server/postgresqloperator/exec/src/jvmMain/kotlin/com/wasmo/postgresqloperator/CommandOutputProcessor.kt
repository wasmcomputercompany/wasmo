package com.wasmo.postgresqloperator

import com.wasmo.events.EventListener
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import okio.BufferedSource

@AssistedInject
class CommandOutputProcessor(
  @Assisted private val name: String,
  @Assisted private val stream: Stream,
  val eventListener: EventListener,
) {
  fun processAll(source: BufferedSource) {
    while (!source.exhausted()) {
      val message = source.readUtf8Line() ?: break
      eventListener.onEvent(CliOutputEvent(name, stream, message))
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      name: String,
      stream: Stream,
    ): CommandOutputProcessor
  }
}
