package com.wasmo.api

import com.wasmo.packaging.AppSlugRegex
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlinx.serialization.Serializable

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
  val slug: AppSlug,
  val launcherLabel: String,
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
