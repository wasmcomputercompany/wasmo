package com.wasmo.api

import com.wasmo.json.ByteStringAsHexSerializer
import kotlinx.serialization.Serializable
import okio.ByteString

@Serializable
data class AppManifest(
  val version: Long,
  val slug: AppSlug,
  val launcherLabel: String,
  val wasmUrl: String,
  @Serializable(with = ByteStringAsHexSerializer::class)
  val wasmSha256: ByteString,
)
