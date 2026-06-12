package dev.wasmo.brevity.gradle

import org.gradle.api.Action

interface BrevityExtension {
  fun generateKotlin(action: Action<BrevityTask>)
}
