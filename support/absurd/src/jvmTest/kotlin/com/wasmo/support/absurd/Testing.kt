package com.wasmo.support.absurd

import assertk.Assert
import assertk.assertions.support.fail
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.Channel

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
