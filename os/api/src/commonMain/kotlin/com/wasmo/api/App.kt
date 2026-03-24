package com.wasmo.api

import com.wasmo.identifiers.AppSlug
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class InstallAppRequest(
  /** This is either a URL or a file path. */
  val appManifestAddress: String,
)

@Serializable
data class InstallAppResponse(
  val url: String,
)

@Serializable
data class InstalledAppSnapshot(
  val slug: AppSlug,
  val launcherLabel: String,
  val maskableIconUrl: String,
  val installScheduledAt: Instant,
  val installCompletedAt: Instant? = null,
  val installDeletedAt: Instant? = null,
  val installIncompleteReason: InstallIncompleteReason? = null,
)

enum class InstallIncompleteReason {
  Unknown,
  SourceUnavailable,
  TargetCapacity,
}
