package com.wasmo.client.framework

import kotlinx.coroutines.flow.StateFlow

interface Presenter<M, E> {
  val model: StateFlow<M>

  fun onEvent(event: E)
}
