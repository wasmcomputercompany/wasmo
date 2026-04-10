package com.wasmo.support.absurd

import assertk.Assert
import assertk.assertions.support.fail
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.Channel

fun <T> Channel<T>.receiveAvailable(): List<T> {
  return buildList {
    while (true) {
      val receive = tryReceive()
      if (!receive.isSuccess) break
      add(receive.getOrThrow())
    }
  }
}

fun Assert<TaskResult<*, *>?>.isFailure(
  message: String,
  throwableClass: KClass<*>,
) = given { actual ->
  if (actual == null) {
    fail(TaskResult.Failed::class, null)
  } else if (actual !is TaskResult.Failed<*, *>) {
    fail(TaskResult.Failed::class, actual::class)
  } else if (actual.message != message) {
    fail(message, actual.message)
  } else if (actual.throwableClassName != throwableClass.qualifiedName) {
    fail(throwableClass.qualifiedName, actual.throwableClassName)
  }
}
