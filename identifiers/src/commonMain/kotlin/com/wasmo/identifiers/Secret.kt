package com.wasmo.identifiers

import kotlin.jvm.JvmInline

@JvmInline
value class Secret(val rawUnredactedSecret: String) {
  override fun toString() = "<redacted>"
}
