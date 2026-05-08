package com.wasmo.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

internal val LocalIdGenerator = compositionLocalOf { IdGenerator() }

/**
 * Generate a unique HTML element ID like `id100`.
 */
@Composable
internal fun rememberNextId(): String {
  val idGenerator = LocalIdGenerator.current
  return remember {
    idGenerator.nextId()
  }
}

internal class IdGenerator {
  private var nextId: Int = 1000

  fun nextId() = "id${nextId++}"
}
