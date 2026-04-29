package com.wasmo.identifiers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

data class JobName<P : Any, R : Any>(
  val value: String,
  val paramsSerializer: KSerializer<P>,
  val resultSerializer: KSerializer<R>,
) {
  override fun toString() = value

  companion object {
    inline operator fun <reified P : Any, reified R : Any> invoke(value: String): JobName<P, R> =
      JobName(value, serializer<P>(), serializer<R>())
  }
}
