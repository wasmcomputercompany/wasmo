package com.wasmo.journal.api

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PublishState(
  val publishNeededAt: Instant?,
  val lastPublishedAt: Instant,
)

@Serializable
data object RequestPublishRequest
