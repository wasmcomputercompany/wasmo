package com.wasmo.api

import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/** Between 1 and 15 letters or digits, and the first is not a digit. */
val AppSlugRegex = Regex("[a-z][a-z0-9]{0,14}")

@Serializable
data class InstallAppRequest(
  val manifestUrl: String,
)

@Serializable
data class InstallAppResponse(
  val url: String,
)

@Serializable
data class InstalledApp(
  val label: String,
  val slug: AppSlug,
  val maskableIconUrl: String,
  val installScheduledAt: Instant,
  val installCompletedAt: Instant? = null,
  val installDeletedAt: Instant? = null,
  val installIncompleteReason: InstallIncompleteReason? = null,
)

@Serializable
@JvmInline
value class AppSlug(val value: String) {
  init {
    require(value.matches(AppSlugRegex)) {
      "unexpected app: $value"
    }
  }
}

enum class InstallIncompleteReason {
  Unknown,
  SourceUnavailable,
  TargetCapacity,
}
